package com.badr.infodota.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.badr.infodota.R;
import com.badr.infodota.activity.CounterPickerHeroesSelectActivity;
import com.badr.infodota.api.heroes.Hero;
import com.badr.infodota.api.heroes.TruepickerHero;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ABadretdinov
 * Date: 21.02.14
 * Time: 19:20
 */
public class HeroesSelectAdapter extends BaseAdapter implements Filterable {
    protected ImageLoader imageLoader;
    DisplayImageOptions options;
    private LayoutInflater mInflater;
    private List<TruepickerHero> mHeroes;
    private List<TruepickerHero> allHeroes;
    private List<Integer> allies;
    private List<Integer> enemies;
    private int mode;
    private int alliesColor;
    private int enemiesColor;

    public HeroesSelectAdapter(Context context, List<TruepickerHero> heroes, List<Integer> allies, List<Integer> enemies, int mode) {
        this.mode = mode;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_img)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        allHeroes = heroes;
        mHeroes = heroes != null ? heroes : new ArrayList<TruepickerHero>();
        this.allies = allies;
        this.enemies = enemies;
        enemiesColor = context.getResources().getColor(R.color.enemy_team);
        alliesColor = context.getResources().getColor(R.color.ally_team);
    }

    public int getCount() {
        return mHeroes.size();
    }

    public Hero getItem(int position) {
        return mHeroes.get(position);
    }

    public long getItemId(int position) {
        return mHeroes.get(position).getId();
    }

    public void setSelectedHero(int selectedHero) {
        if (mode == CounterPickerHeroesSelectActivity.ALLY) {
            if (allies.contains(selectedHero)) {
                allies.remove(Integer.valueOf(selectedHero));
            } else if (allies.size() < 4) {
                allies.add(selectedHero);
                enemies.remove(Integer.valueOf(selectedHero));
            }
        } else {
            if (enemies.contains(selectedHero)) {
                enemies.remove(Integer.valueOf(selectedHero));
            } else if (enemies.size() < 5) {
                enemies.add(selectedHero);
                allies.remove(Integer.valueOf(selectedHero));
            }
        }
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        HeroHolder holder;
        if (convertView == null) {
            vi = mInflater.inflate(R.layout.hero_row, parent, false);
            holder = new HeroHolder();
            holder.name = (TextView) vi.findViewById(R.id.name);
            holder.image = (ImageView) vi.findViewById(R.id.img);
            vi.setTag(holder);
        } else {
            holder = (HeroHolder) vi.getTag();
        }
        Hero hero = getItem(position);
        holder.name.setText(hero.getLocalizedName());
        if (allies.contains((int) hero.getId())) {
            vi.setBackgroundColor(alliesColor);
        } else if (enemies.contains((int) hero.getId())) {
            vi.setBackgroundColor(enemiesColor);
        } else {
            vi.setBackgroundResource(0);
        }
        imageLoader.displayImage("assets://heroes/" + hero.getDotaId() + "/full.png", holder.image, options);
        return vi;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<Hero> filteredHeroes = new ArrayList<Hero>();
                if (constraint == null) {
                    filterResults.values = allHeroes;
                    filterResults.count = allHeroes.size();
                    return filterResults;
                }
                String lowerConstr = constraint.toString().toLowerCase();
                for (Hero hero : allHeroes) {
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
                mHeroes = (ArrayList<TruepickerHero>) results.values;
                if (mHeroes == null) {
                    mHeroes = new ArrayList<TruepickerHero>();
                }
                if (results.count >= 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    public static class HeroHolder {
        TextView name;
        ImageView image;
    }
}
