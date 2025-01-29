package com.thezec.coffeein.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.thezec.coffeein.Models.FunFacts;
import com.thezec.coffeein.Models.QuotesModel;
import com.thezec.coffeein.Models.Recipe;
import com.thezec.coffeein.R;
import com.thezec.coffeein.Utils.SharedPreference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Aleksandar on 3/31/2014.
 */
public class RecipeViewActivity extends Activity {

    @InjectView(R.id.header)
    ImageView imageBackground;
    @InjectView(R.id.drawer)
    View drawer;
    @InjectView(R.id.scroll)
    ScrollView scroll;
    @InjectView(R.id.dim)
    View dim;
    FloatingActionButton fab;

    private List<QuotesModel> quotesList;
    private List<FunFacts> funFactsList;
    private QuotesModel quote;
    private FunFacts funFacts;
    private Recipe recipe;
    private int maxTranslationY;
    private Target target;
    private SharedPreference sharedPreference;
    private Typeface customFont;

    public static float scaleInRange(float valueIn, float baseMin, float baseMax, float limitMin, float limitMax) {

        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);
        ButterKnife.inject(this);
        sharedPreference = SharedPreference.load(this);
        isOnline();

        customFont = Typeface.createFromAsset(getAssets(), "fonts/Mountain-Demo.otf");

        // initialize the GSON parser and prepare the quote list
        Gson gson = new Gson();
        try {
            // get the input stream reader for the assets json file that contains the quote
            InputStream inputStream = getAssets().open("quotes.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // parse the json file to create the list of quotes
            quotesList = gson.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<QuotesModel>>() {
            }.getType());
        } catch (Exception e) {
            // an error occurred, propagate it to the UI for handling
            e.printStackTrace();
            finish();
            return;
        }

        // get a random quote
        quote = quotesList.get(new Random().nextInt(quotesList.size()));

        // initialize the GSON parser and prepare the funFact list
        Gson gsonFunFact = new Gson();
        try {
            // get the input stream reader for the assets json file that contains the quote
            InputStream inputStream = getAssets().open("funFacts.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            // parse the json file to create the list of quotes
            funFactsList = gsonFunFact.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<FunFacts>>() {
            }.getType());
        } catch (Exception e) {
            // an error occurred, propagate it to the UI for handling
            e.printStackTrace();
            finish();
            return;
        }

        // get a random quote
        funFacts = funFactsList.get(new Random().nextInt(funFactsList.size()));

        // get the recipe argument
        String nameFromFirstActivity = getIntent().getStringExtra("jsonRecipe");
        recipe = new Gson().fromJson(nameFromFirstActivity, Recipe.class);

        // load the recipe image into the image view background
        if(isOnline()){
            Picasso.with(this)
                    .load(recipe.getUrl())
                    .placeholder(R.id.header)
                    .into(imageBackground);
        }else{
            Picasso.with(this)
                    .load(R.drawable.coffee_placeholder)
                    .placeholder(R.id.header)
                    .into(imageBackground);
        }

        initDrawer();

        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                imageBackground.setTranslationY(-scroll.getScrollY() / 3);

                float valueIn = (float) scroll.getScrollY() / scroll.getMaxScrollAmount();
                float newScale;
                if (valueIn > 1)
                    newScale = 1.25f;
                else
                    newScale = scaleInRange(
                            valueIn,
                            0f,
                            1f,
                            1f,
                            1.25f);

                imageBackground.setScaleX(newScale);
                imageBackground.setScaleY(newScale);

                float newAlpha;
                if (valueIn > 1)
                    newAlpha = 0.75f;
                else
                    newAlpha = scaleInRange(
                            valueIn,
                            0f,
                            1f,
                            0f,
                            0.75f);

                dim.setAlpha(newAlpha);
            }
        });

        if (checkFavoriteItem(recipe)) {
            fab.setColorNormalResId(R.color.actionbar);
            recipe.setFav(true);

        } else {
            recipe.setFav(false);
            target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                        public void onGenerated(Palette palette) {
                            imageBackground.setImageBitmap(bitmap);
                            int defaultColor = getResources().getColor(R.color.actionbar);
                            fab.setColorNormal(palette.getMutedColor(defaultColor));
                        }
                    });
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            if(isOnline()){
                Picasso.with(this)
                        .load(recipe.getUrl())
                        .into(target);
            }else{
                Picasso.with(this)
                        .load(R.drawable.coffee_placeholder)
                        .into(target);
            }

        }

        //set boolean for firs run
        final boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recipe.isFav()) {
                    sharedPreference.removeFavorite(getBaseContext(), recipe);
                    target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    imageBackground.setImageBitmap(bitmap);
                                    int defaultColor = getResources().getColor(R.color.actionbar);
                                    fab.setColorNormal(palette.getMutedColor(defaultColor));
                                }
                            });
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    };
                    if(isOnline()){
                        Picasso.with(getApplication())
                                .load(recipe.getUrl())
                                .into(target);
                    }else{
                        Picasso.with(getApplication())
                                .load(R.drawable.coffee_placeholder)
                                .into(target);
                    }

                } else {
                    sharedPreference.addFavorite(getBaseContext(), recipe);
                    fab.setColorNormalResId(R.color.actionbar);
                }

                if(isFirstRun){
                    Toast.makeText(RecipeViewActivity.this, "Added to favourites", Toast.LENGTH_LONG).show();
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).commit();
                }
            }
        });
    }

    private void initDrawer() {

        final TextView quotetTitle = (TextView) drawer.findViewById(R.id.quoteTitle);
        final TextView funFactTitle = (TextView) drawer.findViewById(R.id.funFactTitle);
        quotetTitle.setTypeface(customFont);
        funFactTitle.setTypeface(customFont);

        final TextView textRecipe = (TextView) drawer.findViewById(R.id.txt_recipe_view_new);
        textRecipe.setText(recipe.getCoffee() + "\n\n" + recipe.getPreparation());
        textRecipe.setTypeface(customFont);

        final TextView textQuote = (TextView) drawer.findViewById(R.id.quote);
        textQuote.setText(quote.getQuote());
        textQuote.setTypeface(customFont);

        TextView textAuthor = (TextView) drawer.findViewById(R.id.author);
        textAuthor.setText(quote.getBy());
        textAuthor.setTypeface(customFont);

        final TextView textFunFact = (TextView) drawer.findViewById(R.id.fun_fact);
        textFunFact.setText(funFacts.getFunFact());
        textFunFact.setTypeface(customFont);

        fab = (FloatingActionButton) drawer.findViewById(R.id.fab);

        // measure the background and offset the drawer proportionally
        imageBackground.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int backgroundHeight = v.getHeight();
                maxTranslationY = backgroundHeight * 3 / 4;

                LinearLayout.LayoutParams paramsText = (LinearLayout.LayoutParams) textRecipe.getLayoutParams();
                paramsText.setMargins(0, maxTranslationY, 0, 0);
                // always remove the layout listener once measured once
                imageBackground.removeOnLayoutChangeListener(this);
            }
        });

        fab.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
                params.setMargins(0, maxTranslationY - v.getHeight() / 2, 0, 0);
                fab.removeOnLayoutChangeListener(this);
            }
        });
    }

    public boolean checkFavoriteItem(Recipe checkRecipe) {
        boolean check = false;
        List<Recipe> favorites = sharedPreference.getFavorites(this);
        if (favorites != null) {
            for (Recipe re : favorites) {
                if (re.equals(checkRecipe)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}
