package com.badr.infodota.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.badr.infodota.R;
import com.badr.infodota.adapter.holder.HeroHolder;
import com.badr.infodota.api.heroes.Hero;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ABadretdinov
 * Date: 29.08.13
 * Time: 12:25
 */
public class HeroesAdapter extends BaseRecyclerAdapter<Hero, HeroHolder> implements Filterable {
    protected ImageLoader imageLoader;
    DisplayImageOptions options;
    private List<Hero> filtered;

    public HeroesAdapter(List<Hero> heroes) {
        super(heroes);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_img)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        filtered = mData;
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Hero getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public HeroHolder onCreateViewHolder(ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hero_row, parent, false);
        return new HeroHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(HeroHolder holder, int position) {
        Hero hero = getItem(position);
        holder.name.setText(hero.getLocalizedName());
        imageLoader.displayImage("assets://heroes/" + hero.getDotaId() + "/full.png", holder.image, options);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<Hero> filteredHeroes = new ArrayList<Hero>();
                if (constraint == null) {
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                    return filterResults;
                }
                String lowerConstr = constraint.toString().toLowerCase();
                for (Hero hero : mData) {
                    if (hero.getLocalizedName().toLowerCase().contains(lowerConstr) || hero.getName().toLowerCase().contains(lowerConstr)) {
                        filteredHeroes.add(hero);
                    }
                }
                filterResults.count = filteredHeroes.size();
                filterResults.values = filteredHeroes;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (ArrayList<Hero>) results.values;
                if (filtered == null) {
                    filtered = new ArrayList<Hero>();
                }
                notifyDataSetChanged();
            }
        };
    }
}
