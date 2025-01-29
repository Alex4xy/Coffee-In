package com.thezec.coffeein.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.squareup.picasso.Picasso;
import com.thezec.coffeein.Adapters.GridAdapter;
import com.thezec.coffeein.Adapters.NavigationdDrawerAdapter;
import com.thezec.coffeein.Adapters.RecipesListViewAdapter;
import com.thezec.coffeein.Models.NavigationDrawerListModel;
import com.thezec.coffeein.Models.Recipe;
import com.thezec.coffeein.R;
import com.thezec.coffeein.Utils.SharedPreference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.drawer_list_categories)
    ListView drawerCategoriesList;
    @InjectView(R.id.drawer_list_recipes)
    ListView drawerListRecipes;
    @InjectView(R.id.searchViewHomePage)
    SearchView search;
    @InjectView(R.id.header_drawer)
    TextView header;
    @InjectView(R.id.favourites)
    TextView favourites;
    @InjectView(R.id.drawer_list_favourites)
    ListView favouritesList;
    @InjectView(R.id.random)
    TextView randomize;
    @InjectView(R.id.drawer_list_searchList)
    ListView searchList;
    @InjectView(R.id.activity_main_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.bottom_border)
    TextView bottomBorder;
    @InjectView(R.id.back_image)
    ImageView backImage;
    @InjectView(R.id.back_layout)
    LinearLayout backLayout;

    private ActionBarDrawerToggle drawerToggle;
    private String[] drawerTitlesCategories;
    private TypedArray iconsCategories;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private List<Recipe> gridList;
    private List<Recipe> favorites;
    private List<Recipe> recipeList;
    private Filter filter;
    private RecipesListViewAdapter recipeAdapter;
    private NavigationdDrawerAdapter navAdapter;
    private boolean isClassic = false;
    private boolean isLatte = false;
    private boolean isFrappe = false;
    private boolean isWorld = false;
    private boolean isExotic = false;
    private boolean isCocktail = false;
    private boolean isDrawerClosed = true;
    private boolean isFavList = false;
    private boolean isRecipeList = false;
    private boolean isSearchList = false;
    private Recipe recipe;
    private GridAdapter gridAdapter;
    private SharedPreference sharedPreference;
    private Typeface customFont;
    private View gridHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        sharedPreference = SharedPreference.load(this);

        //set boolean for first run
        final boolean isFirstRunMain = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRunMain", true);
        if (isFirstRunMain) {
            Toast.makeText(MainActivity.this, "Swipe down to shuffle", Toast.LENGTH_LONG).show();
            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRunMain", false).commit();
        }

        //set custom font
        customFont = Typeface.createFromAsset(getAssets(), "fonts/Mountain-Demo.otf");
        header.setTypeface(customFont);
        favourites.setTypeface(customFont);
        randomize.setTypeface(customFont);

        //get json from assets for grid
        Gson gson = new Gson();
        try {
            // get the input stream reader for the assets json file that contains the recipes
            InputStream inputStream = getAssets().open("recipesJson.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // parse the json file to create the list of recipes
            gridList = gson.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<Recipe>>() {
            }.getType());
        } catch (Exception e) {
            // an error occurred, propagate it to the UI for handling
            e.printStackTrace();
            finish();
            return;
        }

        //set grid
        gridAdapter = new GridAdapter(this, gridList);
        gridHeader = LayoutInflater.from(this).inflate(R.layout.grid_header, null, false);
        setGridHeader();
        final jp.co.recruit_mp.android.widget.HeaderFooterGridView gridList = (jp.co.recruit_mp.android.widget.HeaderFooterGridView) findViewById(R.id.gridList);
        gridList.addHeaderView(gridHeader);
        gridList.setNumColumns(2);
        gridList.setAdapter(gridAdapter);

        gridList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setOnClick(parent, position);
            }
        });

        //disable swipe if its not first visible item
        gridList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (gridList != null && gridList.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = gridList.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = gridList.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        //set on refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shuffleList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.black_text);

        //get json from assets for others
        Gson gsonRecipe = new Gson();
        try {
            // get the input stream reader for the assets json file that contains the recipe
            InputStream inputStream = getAssets().open("recipesJson.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // parse the json file to create the list of recipe
            recipeList = gsonRecipe.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<Recipe>>() {
            }.getType());
        } catch (Exception e) {
            // an error occurred, propagate it to the UI for handling
            e.printStackTrace();
            finish();
            return;
        }

        recipeAdapter = new RecipesListViewAdapter(this, recipeList);
        searchList.setAdapter(recipeAdapter);
        searchList.setVisibility(View.GONE);

        //set categories icon and titles
        drawerTitlesCategories = getResources().getStringArray(R.array.navigation_drawer_categories);
        iconsCategories = getResources().obtainTypedArray(R.array.navigatin_drawer_icons);
        //set categories list
        navAdapter = new NavigationdDrawerAdapter(this, generateDataCategories());
        drawerCategoriesList.setAdapter(navAdapter);

        drawerCategoriesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectDrawerCategories(position);
            }
        });

        //set recipe list on click
        drawerListRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setOnClick(parent, position);
            }
        });

        //set search list on click
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setOnClick(parent, position);
            }
        });

        //set favourites on click
        favourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDrawerItem(0);
            }
        });

        //set random on click
        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDrawerItem(1);
            }
        });

        //set on search click
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                drawerCategoriesList.setVisibility(View.GONE);
                drawerListRecipes.setVisibility(View.GONE);
                favouritesList.setVisibility(View.GONE);
                searchList.setVisibility(View.VISIBLE);
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                isSearchList = true;
            }
        });

        //Listen for Open and Close Events
        mTitle = mDrawerTitle = getTitle();

        if (toolbar != null) {
            toolbar.setTitle(mTitle);
            setSupportActionBar(toolbar);
        }

        initDrawer();
        setDrawerRecipeBackButton();
        setupSearchView();

    }

    private void setFavList() {
        //set the favourites list
        favorites = sharedPreference.getFavorites(this);
        if (favorites != null) {
            recipeAdapter = new RecipesListViewAdapter(this, favorites);
            favouritesList.setAdapter(recipeAdapter);
            favouritesList.setVisibility(View.GONE);

            favouritesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View arg1,
                                        int position, long arg3) {
                    setOnClick(parent, position);
                }
            });
        }
    }

    private void setDrawerRecipeBackButton() {
        //set recipes list header back navigation
        backLayout.setVisibility(View.GONE);
        bottomBorder.setVisibility(View.GONE);
        header.setText("Back to Categories");

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerListRecipes.setVisibility(View.GONE);
                drawerCategoriesList.setVisibility(View.VISIBLE);
                backLayout.setVisibility(View.GONE);
                bottomBorder.setVisibility(View.GONE);
                favouritesList.setVisibility(View.GONE);
                searchList.setVisibility(View.GONE);
                setTitle(mDrawerTitle);
                isFavList = false;
                isRecipeList = false;
                isSearchList = false;

                isClassic = false;
                isLatte = false;
                isFrappe = false;
                isWorld = false;
                isExotic = false;
                isCocktail = false;
            }
        });

        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerListRecipes.setVisibility(View.GONE);
                drawerCategoriesList.setVisibility(View.VISIBLE);
                backLayout.setVisibility(View.GONE);
                bottomBorder.setVisibility(View.GONE);
                favouritesList.setVisibility(View.GONE);
                searchList.setVisibility(View.GONE);
                setTitle(mDrawerTitle);
                isFavList = false;
                isRecipeList = false;
                isSearchList = false;

                isClassic = false;
                isLatte = false;
                isFrappe = false;
                isWorld = false;
                isExotic = false;
                isCocktail = false;
            }
        });
    }

    private void setGridHeader() {
        //set header recipe name
        TextView headerGridRecipe = (TextView) gridHeader.findViewById(R.id.headerGridRecipe);
        recipe = gridList.get(new Random().nextInt(gridList.size()));
        headerGridRecipe.setText(recipe.getCoffee());
        headerGridRecipe.setTypeface(customFont);

        //set header recipe image
        final ImageView headerGridImage = (ImageView) gridHeader.findViewById(R.id.headerGridImage);
        Picasso.with(this)
                .load(recipe.getUrl())
                .fit()
                .centerCrop()
                .placeholder(R.id.headerGridImage)
                .into(headerGridImage);

        headerGridImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the clicked item
                Recipe clickedItem = recipe;
                String jsonRecipe = new Gson().toJson(clickedItem, Recipe.class);
                // make new activity
                Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
                viewActivity.putExtra("jsonRecipe", jsonRecipe);
                startActivity(viewActivity);
            }
        });
    }

    private void initDrawer() {
        //setup navigation drawer
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // when drawer closed
                toolbar.setTitle(mTitle);
                isDrawerClosed = true;
                setTitle(R.string.app_name);
                drawerView.setClickable(false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // when drawer open
                toolbar.setTitle(mTitle);
                isDrawerClosed = false;
                if (isClassic) {
                    setTitle("Classic");
                } else if (isLatte) {
                    setTitle("Latte");
                } else if (isWorld) {
                    setTitle("World Coffee");
                } else if (isExotic) {
                    setTitle("Exotic");
                } else if (isCocktail) {
                    setTitle("Coffee Cocktail");
                }
                drawerView.setClickable(true);
            }
        };

        // setDrawerlisterner
        drawerLayout.setDrawerListener(drawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectDrawerItem(int position) {

        switch (position) {
            case 0:
                favouritesList.setVisibility(View.VISIBLE);
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                drawerListRecipes.setVisibility(View.GONE);
                searchList.setVisibility(View.GONE);
                isFavList = true;
                if (favorites == null) {
                    Toast.makeText(getBaseContext(), "Favourites is Empty", Toast.LENGTH_SHORT).show();
                } else {
                    if (favorites.size() == 0) {
                        Toast.makeText(getBaseContext(), "Favourites is Empty", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case 1:
                recipe = gridList.get(new Random().nextInt(gridList.size()));
                String jsonRecipe = new Gson().toJson(recipe, Recipe.class);
                // make new activity
                Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
                viewActivity.putExtra("jsonRecipe", jsonRecipe);
                startActivity(viewActivity);
                break;

            default:
                break;
        }
    }

    private void selectDrawerCategories(int position) {

        switch (position) {
            case 0:
                isRecipeList = true;
                isClassic = true;
                List<Recipe> classic = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("classics")) {
                        classic.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, classic));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("Classic");
                break;

            case 1:
                isRecipeList = true;
                isLatte = true;
                List<Recipe> latte = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("lattes")) {
                        latte.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, latte));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("Latte");
                break;

            case 2:
                isRecipeList = true;
                isFrappe = true;
                List<Recipe> frappe = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("frappes")) {
                        frappe.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, frappe));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("Frappe");
                break;

            case 3:
                isRecipeList = true;
                isWorld = true;
                List<Recipe> world = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("world")) {
                        world.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, world));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("World Coffee");
                break;

            case 4:
                isRecipeList = true;
                isExotic = true;
                List<Recipe> exotic = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("exotic")) {
                        exotic.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, exotic));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("Exotic");
                break;

            case 5:
                isRecipeList = true;
                isCocktail = true;
                List<Recipe> cocktail = new ArrayList<Recipe>();
                for (int i = 0; i < recipeList.size(); i++) {
                    recipe = recipeList.get(i);
                    if (recipe.getType().equals("cocktails")) {
                        cocktail.add(recipe);
                    }
                }
                drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, cocktail));
                backLayout.setVisibility(View.VISIBLE);
                bottomBorder.setVisibility(View.VISIBLE);
                drawerListRecipes.setVisibility(View.VISIBLE);
                drawerCategoriesList.setVisibility(View.GONE);
                setTitle("Coffee Cocktail");
                break;

            default:
                break;
        }
    }

    private ArrayList<NavigationDrawerListModel> generateDataCategories() {
        ArrayList<NavigationDrawerListModel> navItem = new ArrayList<NavigationDrawerListModel>();
        for (int i = 0; i < 6; i++) {
            navItem.add(new NavigationDrawerListModel(drawerTitlesCategories[i], iconsCategories.getResourceId(i, -1), true));
        }
        return navItem;
    }

    private void shuffleList() {
        Collections.shuffle(gridList);
        gridAdapter.notifyDataSetChanged();
        setGridHeader();
    }

    private void setupSearchView() {

        filter = recipeAdapter.getFilter();

        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setSubmitButtonEnabled(false);
        search.setOnQueryTextListener(this);
        search.setIconified(true);

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) search.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.DKGRAY);
        searchEditText.setTypeface(customFont);
        Drawable searchIcon = getApplicationContext().getResources().getDrawable(R.drawable.ic_action_search);
        searchIcon.setBounds(0, 0, 12, 12);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter.filter(newText);
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!isDrawerClosed) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFavList();
        if (isFavList) {
            drawerListRecipes.setVisibility(View.GONE);
            drawerCategoriesList.setVisibility(View.GONE);
            searchList.setVisibility(View.GONE);
            favouritesList.setVisibility(View.VISIBLE);
        } else if (isRecipeList) {
            favouritesList.setVisibility(View.GONE);
            drawerCategoriesList.setVisibility(View.GONE);
            searchList.setVisibility(View.GONE);
            drawerListRecipes.setVisibility(View.VISIBLE);
        } else if (isSearchList) {
            favouritesList.setVisibility(View.GONE);
            drawerCategoriesList.setVisibility(View.GONE);
            drawerListRecipes.setVisibility(View.GONE);
            searchList.setVisibility(View.VISIBLE);
        } else {
            favouritesList.setVisibility(View.GONE);
            drawerListRecipes.setVisibility(View.GONE);
            drawerCategoriesList.setVisibility(View.VISIBLE);
        }
    }

    private void setOnClick(AdapterView parent, int position) {
        //get clicked item
        Recipe clickedItem = (Recipe) parent.getAdapter().getItem(position);
        String jsonRecipe = new Gson().toJson(clickedItem, Recipe.class);
        // make new activity
        Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
        viewActivity.putExtra("jsonRecipe", jsonRecipe);
        startActivity(viewActivity);
    }
}