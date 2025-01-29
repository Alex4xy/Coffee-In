package com.thezec.coffeein.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.Gson;
import com.thezec.coffeein.Models.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Aleksandar on 7/3/2014.
 */
public class SharedPreference {

    public static final String FAVORITES = "Product_Favorite";
    private static final String PREFS_NAME = "PRODUCT_APP";
    private final SharedPreferences sharedPreference;


    public SharedPreference(SharedPreferences sharedPreferences) {
        this.sharedPreference = sharedPreferences;
    }

    public static SharedPreference load(Context context) {
        return new SharedPreference(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE));
    }

    // This four methods are used for maintaining favorites.
    public void saveFavorites(Context context, List<Recipe> favorites) {
        SharedPreferences settings;
        Editor editor;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORITES, jsonFavorites);

        editor.commit();
    }

    public void addFavorite(Context context, Recipe recipe) {
        List<Recipe> favorites = getFavorites(context);
        if (favorites == null)
            favorites = new ArrayList<Recipe>();
        if (!favorites.contains(recipe)) {
            favorites.add(recipe);
            recipe.setFav(true);

        }
        saveFavorites(context, favorites);
    }

    public void removeFavorite(Context context, Recipe recipe) {

        List<Recipe> favorites = getFavorites(context);
        if (favorites != null) {
            for (int i = 0; i < favorites.size(); i++) {
                Recipe re = favorites.get(i);
                if (re.getId() == recipe.getId()) {
                    favorites.remove(recipe);
                    recipe.setFav(false);
                    break;
                }
            }
        }
        saveFavorites(context, favorites);
    }

    public ArrayList<Recipe> getFavorites(Context context) {
        SharedPreferences settings;
        List<Recipe> favorites;

        settings = context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(FAVORITES)) {
            String jsonFavorites = settings.getString(FAVORITES, null);
            Gson gson = new Gson();
            Recipe[] favoriteItems = gson.fromJson(jsonFavorites,
                    Recipe[].class);

            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<Recipe>(favorites);
        } else
            return null;

        return (ArrayList<Recipe>) favorites;
    }
}