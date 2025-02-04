package com.thezec.coffeein.Activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
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

public class RecipeViewActivity extends Activity {

    // Views (now found manually via findViewById)
    private ImageView imageBackground;
    private View drawer;
    private ScrollView scroll;
    private View dim;
    private com.github.clans.fab.FloatingActionButton fab;

    // Data fields
    private List<QuotesModel> quotesList;
    private List<FunFacts> funFactsList;
    private QuotesModel quote;
    private FunFacts funFacts;
    private Recipe recipe;
    private int maxTranslationY;
    private Target target;
    private SharedPreference sharedPreference;
    private Typeface customFont;

    /**
     * Scales a value in the range [baseMin, baseMax] to a new value
     * in the range [limitMin, limitMax].
     */
    public static float scaleInRange(float valueIn, float baseMin, float baseMax, float limitMin, float limitMax) {
        return ((limitMax - limitMin) * (valueIn - baseMin) / (baseMax - baseMin)) + limitMin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view);

        // Manually find views (ButterKnife is no longer used)
        imageBackground = findViewById(R.id.header);
        drawer = findViewById(R.id.drawer);
        scroll = findViewById(R.id.scroll);
        dim = findViewById(R.id.dim);
        // fab will be found later inside the drawer

        sharedPreference = SharedPreference.load(this);
        // (Optional) check connectivity early if desired
        isOnline();

        customFont = Typeface.createFromAsset(getAssets(), "fonts/Mountain-Demo.otf");

        // Load quotes list from assets/quotes.txt
        Gson gson = new Gson();
        try {
            InputStream inputStream = getAssets().open("quotes.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            quotesList = gson.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<QuotesModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }
        // Get a random quote
        quote = quotesList.get(new Random().nextInt(quotesList.size()));

        // Load fun facts list from assets/funFacts.txt
        try {
            InputStream inputStream = getAssets().open("funFacts.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            funFactsList = gson.fromJson(new JsonReader(inputStreamReader), new TypeToken<List<FunFacts>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            finish();
            return;
        }
        funFacts = funFactsList.get(new Random().nextInt(funFactsList.size()));

        // Get recipe from intent extra "jsonRecipe"
        String jsonRecipe = getIntent().getStringExtra("jsonRecipe");
        recipe = new Gson().fromJson(jsonRecipe, Recipe.class);

        // Load the recipe image into the imageBackground using Picasso
        if (isOnline()) {
            Picasso.get()
                    .load(recipe.getUrl())
                    .placeholder(R.drawable.coffee_placeholder)
                    .into(imageBackground);
        } else {
            Picasso.get()
                    .load(R.drawable.coffee_placeholder)
                    .placeholder(R.drawable.coffee_placeholder)
                    .into(imageBackground);
        }

        // Initialize the drawer contents (which will also find the fab inside the drawer)
        initDrawer();

        // Add a scroll listener for parallax and scaling effects
        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                imageBackground.setTranslationY(-scroll.getScrollY() / 3f);

                float valueIn = (float) scroll.getScrollY() / scroll.getMaxScrollAmount();
                float newScale = (valueIn > 1) ? 1.25f : scaleInRange(valueIn, 0f, 1f, 1f, 1.25f);
                imageBackground.setScaleX(newScale);
                imageBackground.setScaleY(newScale);

                float newAlpha = (valueIn > 1) ? 0.75f : scaleInRange(valueIn, 0f, 1f, 0f, 0.75f);
                dim.setAlpha(newAlpha);
            }
        });

        // Set up favorite state and adjust fab colors accordingly
        if (checkFavoriteItem(recipe)) {
            fab.setColorNormalResId(R.color.actionbar);
            recipe.setFav(true);
        } else {
            recipe.setFav(false);
            target = new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

                    androidx.palette.graphics.Palette.from(bitmap).generate(palette -> {
                        imageBackground.setImageBitmap(bitmap);
                        int defaultColor = getResources().getColor(R.color.actionbar);
                        fab.setColorNormal(palette.getMutedColor(defaultColor));
                    });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
            if (isOnline()) {
                Picasso.get()
                        .load(recipe.getUrl())
                        .into(target);
            } else {
                Picasso.get()
                        .load(R.drawable.coffee_placeholder)
                        .into(target);
            }
        }

        final boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        // Set fab click listener to add or remove favorites
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recipe.isFav()) {
                    sharedPreference.removeFavorite(getBaseContext(), recipe);
                    target = new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            androidx.palette.graphics.Palette.from(bitmap).generate(palette -> {
                                imageBackground.setImageBitmap(bitmap);
                                int defaultColor = getResources().getColor(R.color.actionbar);
                                fab.setColorNormal(palette.getMutedColor(defaultColor));
                            });
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }


                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    };
                    if (isOnline()) {
                        Picasso.get()
                                .load(recipe.getUrl())
                                .into(target);
                    } else {
                        Picasso.get()
                                .load(R.drawable.coffee_placeholder)
                                .into(target);
                    }
                } else {
                    sharedPreference.addFavorite(getBaseContext(), recipe);
                    fab.setColorNormalResId(R.color.actionbar);
                }

                if (isFirstRun) {
                    Toast.makeText(RecipeViewActivity.this, "Added to favourites", Toast.LENGTH_LONG).show();
                    getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                            .edit().putBoolean("isFirstRun", false).commit();
                }
            }
        });
    }

    private void initDrawer() {
        // Find views inside the drawer layout
        TextView quoteTitle = drawer.findViewById(R.id.quoteTitle);
        TextView funFactTitle = drawer.findViewById(R.id.funFactTitle);
        quoteTitle.setTypeface(customFont);
        funFactTitle.setTypeface(customFont);

        TextView textRecipe = drawer.findViewById(R.id.txt_recipe_view_new);
        textRecipe.setText(recipe.getCoffee() + "\n\n" + recipe.getPreparation());
        textRecipe.setTypeface(customFont);

        TextView textQuote = drawer.findViewById(R.id.quote);
        textQuote.setText(quote.getQuote());
        textQuote.setTypeface(customFont);

        TextView textAuthor = drawer.findViewById(R.id.author);
        textAuthor.setText(quote.getBy());
        textAuthor.setTypeface(customFont);

        TextView textFunFact = drawer.findViewById(R.id.fun_fact);
        textFunFact.setText(funFacts.getFunFact());
        textFunFact.setTypeface(customFont);

        // Find the FloatingActionButton from the drawer
        fab = drawer.findViewById(R.id.fab);

        // Adjust layout margins for the background image and fab
        imageBackground.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int backgroundHeight = v.getHeight();
                maxTranslationY = backgroundHeight * 3 / 4;

                LinearLayout.LayoutParams paramsText = (LinearLayout.LayoutParams) textRecipe.getLayoutParams();
                paramsText.setMargins(0, maxTranslationY, 0, 0);
                imageBackground.removeOnLayoutChangeListener(this);
            }
        });

        fab.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) fab.getLayoutParams();
                params.setMargins(0, maxTranslationY - v.getHeight() / 2, 0, 0);
                fab.removeOnLayoutChangeListener(this);
            }
        });
    }

    /**
     * Checks whether the given recipe is present in the favorites.
     */
    public boolean checkFavoriteItem(Recipe checkRecipe) {
        List<Recipe> favorites = sharedPreference.getFavorites(this);
        if (favorites != null) {
            for (Recipe re : favorites) {
                if (re.equals(checkRecipe)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the device is connected (or connecting) to a network.
     */
    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
}