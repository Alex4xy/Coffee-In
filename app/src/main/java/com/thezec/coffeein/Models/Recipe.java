package com.thezec.coffeein.Models;

/**
 * Created by Aleksandar on 4/24/2014.
 */
public class Recipe {

    public int id;
    private String coffee;
    private String preparation;
    private String type;
    private boolean isFav;
    private String url;

    public Recipe(String coffee, String preparation, int id, String url) {
        this.id = id;
        this.coffee = coffee;
        this.preparation = preparation;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean isFav) {
        this.isFav = isFav;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCoffee() {
        return coffee;
    }

    public void setCoffee(String mName) {
        this.coffee = mName;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Recipe other = (Recipe) obj;
        if (id != other.id)
            return false;
        return true;

    }
}




