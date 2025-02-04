package com.thezec.coffeein.Activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.squareup.picasso.Picasso;
import com.thezec.coffeein.Adapters.GridAdapter;
import com.thezec.coffeein.Adapters.NavigationDrawerAdapter;
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

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ListView drawerCategoriesList, drawerListRecipes, favouritesList, searchList;
    private SearchView search;
    private TextView header, favourites, randomize, bottomBorder;
    private ImageView backImage;
    private LinearLayout backLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GridView gridView;
    private View gridHeader;

    private ActionBarDrawerToggle drawerToggle;
    private String[] drawerTitlesCategories;
    private TypedArray iconsCategories;
    private CharSequence mDrawerTitle, mTitle;
    private List<Recipe> gridList, recipeList, favorites;
    private android.widget.Filter filter;
    private RecipesListViewAdapter recipeAdapter;
    private NavigationDrawerAdapter navAdapter;
    private boolean isClassic, isLatte, isFrappe, isWorld, isExotic, isCocktail;
    private boolean isDrawerClosed = true, isFavList, isRecipeList, isSearchList;
    private Recipe recipe;
    private GridAdapter gridAdapter;
    private SharedPreference sharedPreference;
    private Typeface customFont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerCategoriesList = findViewById(R.id.drawer_list_categories);
        drawerListRecipes = findViewById(R.id.drawer_list_recipes);
        search = findViewById(R.id.searchViewHomePage);
        header = findViewById(R.id.header_drawer);
        favourites = findViewById(R.id.favourites);
        favouritesList = findViewById(R.id.drawer_list_favourites);
        randomize = findViewById(R.id.random);
        searchList = findViewById(R.id.drawer_list_searchList);
        swipeRefreshLayout = findViewById(R.id.activity_main_swipe_refresh_layout);
        bottomBorder = findViewById(R.id.bottom_border);
        backImage = findViewById(R.id.back_image);
        backLayout = findViewById(R.id.back_layout);
        gridView = findViewById(R.id.gridList);
        gridHeader = findViewById(R.id.gridHeader);

        sharedPreference = SharedPreference.load(this);

        SharedPreferences prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        boolean isFirstRunMain = prefs.getBoolean("isFirstRunMain", true);
        if (isFirstRunMain) {
            Toast.makeText(this, "Swipe down to shuffle", Toast.LENGTH_LONG).show();
            prefs.edit().putBoolean("isFirstRunMain", false).apply();
        }

        customFont = Typeface.createFromAsset(getAssets(), "fonts/Mountain-Demo.otf");
        header.setTypeface(customFont);
        favourites.setTypeface(customFont);
        randomize.setTypeface(customFont);

        Gson gson = new Gson();
        try {
            InputStream in = getAssets().open("recipesJson.txt");
            InputStreamReader reader = new InputStreamReader(in);
            gridList = gson.fromJson(new JsonReader(reader), new TypeToken<List<Recipe>>() {}.getType());
        } catch (Exception e) {
            finish();
            return;
        }

        gridAdapter = new GridAdapter(this, gridList);
        gridView.setNumColumns(2);
        gridView.setAdapter(gridAdapter);

        setGridHeader();

        gridView.setOnItemClickListener((parent, view, position, id) -> openRecipeDetail(parent, position));

        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) { }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (gridView != null && gridView.getChildCount() > 0) {
                    boolean firstItemVisible = gridView.getFirstVisiblePosition() == 0;
                    View firstChild = gridView.getChildAt(0);
                    boolean topVisible = firstChild != null && firstChild.getTop() == 0;
                    enable = firstItemVisible && topVisible;
                }
                swipeRefreshLayout.setEnabled(enable);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> {
                shuffleList();
                swipeRefreshLayout.setRefreshing(false);
            }, 2000);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.black_text);

        try {
            InputStream in = getAssets().open("recipesJson.txt");
            InputStreamReader reader = new InputStreamReader(in);
            recipeList = gson.fromJson(new JsonReader(reader), new TypeToken<List<Recipe>>() {}.getType());
        } catch (Exception e) {
            finish();
            return;
        }

        recipeAdapter = new RecipesListViewAdapter(this, recipeList);
        searchList.setAdapter(recipeAdapter);
        searchList.setVisibility(View.GONE);

        drawerTitlesCategories = getResources().getStringArray(R.array.navigation_drawer_categories);
        iconsCategories = getResources().obtainTypedArray(R.array.navigatin_drawer_icons);
        navAdapter = new NavigationDrawerAdapter(this, generateDataCategories());
        drawerCategoriesList.setAdapter(navAdapter);

        drawerCategoriesList.setOnItemClickListener((adapterView, view, position, id) -> selectDrawerCategories(position));

        drawerListRecipes.setOnItemClickListener((parent, view, position, id) -> openRecipeDetail(parent, position));

        searchList.setOnItemClickListener((parent, view, position, id) -> openRecipeDetail(parent, position));

        favourites.setOnClickListener(v -> selectDrawerItem(0));

        randomize.setOnClickListener(v -> selectDrawerItem(1));

        search.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            drawerCategoriesList.setVisibility(View.GONE);
            drawerListRecipes.setVisibility(View.GONE);
            favouritesList.setVisibility(View.GONE);
            searchList.setVisibility(View.VISIBLE);
            backLayout.setVisibility(View.VISIBLE);
            bottomBorder.setVisibility(View.VISIBLE);
            isSearchList = true;
        });

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
        favorites = sharedPreference.getFavorites(this);
        if (favorites != null) {
            recipeAdapter = new RecipesListViewAdapter(this, favorites);
            favouritesList.setAdapter(recipeAdapter);
            favouritesList.setVisibility(View.GONE);
            favouritesList.setOnItemClickListener((parent, view, position, id) -> openRecipeDetail(parent, position));
        }
    }

    private void setDrawerRecipeBackButton() {
        backLayout.setVisibility(View.GONE);
        bottomBorder.setVisibility(View.GONE);
        header.setText("Back to Categories");

        View.OnClickListener backListener = v -> {
            drawerListRecipes.setVisibility(View.GONE);
            drawerCategoriesList.setVisibility(View.VISIBLE);
            backLayout.setVisibility(View.GONE);
            bottomBorder.setVisibility(View.GONE);
            favouritesList.setVisibility(View.GONE);
            searchList.setVisibility(View.GONE);
            setTitle(mDrawerTitle);
            isFavList = isRecipeList = isSearchList = false;
            isClassic = isLatte = isFrappe = isWorld = isExotic = isCocktail = false;
        };
        header.setOnClickListener(backListener);
        backImage.setOnClickListener(backListener);
    }

    private void setGridHeader() {
        TextView headerGridRecipe = gridHeader.findViewById(R.id.headerGridRecipe);
        recipe = gridList.get(new Random().nextInt(gridList.size()));
        headerGridRecipe.setText(recipe.getCoffee());
        headerGridRecipe.setTypeface(customFont);
        ImageView headerGridImage = gridHeader.findViewById(R.id.headerGridImage);
        Picasso.get()
                .load(recipe.getUrl())
                .fit()
                .centerCrop()
                .placeholder(R.drawable.coffee_placeholder)
                .into(headerGridImage);
        headerGridImage.setOnClickListener(v -> {
            String jsonRecipe = new Gson().toJson(recipe, Recipe.class);
            Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
            viewActivity.putExtra("jsonRecipe", jsonRecipe);
            startActivity(viewActivity);
        });
    }

    private void initDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.txt_open, R.string.txt_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                toolbar.setTitle(mTitle);
                isDrawerClosed = true;
                setTitle(R.string.app_name);
                drawerView.setClickable(false);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
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
        drawerLayout.addDrawerListener(drawerToggle);
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
                if (favorites == null || favorites.isEmpty()) {
                    Toast.makeText(getBaseContext(), "Favourites is Empty", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                recipe = gridList.get(new Random().nextInt(gridList.size()));
                String jsonRecipe = new Gson().toJson(recipe, Recipe.class);
                Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
                viewActivity.putExtra("jsonRecipe", jsonRecipe);
                startActivity(viewActivity);
                break;
            default:
                break;
        }
    }

    private void selectDrawerCategories(int position) {
        List<Recipe> listToShow = new ArrayList<>();
        switch (position) {
            case 0:
                isRecipeList = true;
                isClassic = true;
                for (Recipe r : recipeList) {
                    if ("classics".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("Classic");
                break;
            case 1:
                isRecipeList = true;
                isLatte = true;
                for (Recipe r : recipeList) {
                    if ("lattes".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("Latte");
                break;
            case 2:
                isRecipeList = true;
                isFrappe = true;
                for (Recipe r : recipeList) {
                    if ("frappes".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("Frappe");
                break;
            case 3:
                isRecipeList = true;
                isWorld = true;
                for (Recipe r : recipeList) {
                    if ("world".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("World Coffee");
                break;
            case 4:
                isRecipeList = true;
                isExotic = true;
                for (Recipe r : recipeList) {
                    if ("exotic".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("Exotic");
                break;
            case 5:
                isRecipeList = true;
                isCocktail = true;
                for (Recipe r : recipeList) {
                    if ("cocktails".equals(r.getType()))
                        listToShow.add(r);
                }
                setTitle("Coffee Cocktail");
                break;
            default:
                break;
        }
        drawerListRecipes.setAdapter(new RecipesListViewAdapter(this, listToShow));
        backLayout.setVisibility(View.VISIBLE);
        bottomBorder.setVisibility(View.VISIBLE);
        drawerListRecipes.setVisibility(View.VISIBLE);
        drawerCategoriesList.setVisibility(View.GONE);
    }

    private ArrayList<NavigationDrawerListModel> generateDataCategories() {
        ArrayList<NavigationDrawerListModel> navItem = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            navItem.add(new NavigationDrawerListModel(drawerTitlesCategories[i],
                    iconsCategories.getResourceId(i, -1), true));
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
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        search.setSubmitButtonEnabled(false);
        search.setOnQueryTextListener(this);
        search.setIconified(true);
      //  int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
     //   EditText searchEditText = search.findViewById(searchSrcTextId);
//        searchEditText.setTextColor(Color.BLACK);
      //  searchEditText.setHintTextColor(Color.DKGRAY);
    //    searchEditText.setTypeface(customFont);
        Drawable searchIcon = getResources().getDrawable(R.drawable.ic_action_search);
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

    private void openRecipeDetail(AdapterView<?> parent, int position) {
        Recipe clickedItem = (Recipe) parent.getAdapter().getItem(position);
        String jsonRecipe = new Gson().toJson(clickedItem, Recipe.class);
        Intent viewActivity = new Intent(MainActivity.this, RecipeViewActivity.class);
        viewActivity.putExtra("jsonRecipe", jsonRecipe);
        startActivity(viewActivity);
    }
}
