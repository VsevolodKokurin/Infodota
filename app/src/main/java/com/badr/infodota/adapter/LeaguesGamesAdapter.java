package com.badr.infodota.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.badr.infodota.R;
import com.badr.infodota.api.joindota.MatchItem;
import com.badr.infodota.util.DateUtils;
import com.badr.infodota.view.PinnedSectionListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ABadretdinov
 * Date: 22.04.14
 * Time: 18:36
 */
public class LeaguesGamesAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter {
    DisplayImageOptions options;
    //todo options with cache and default img
    private LayoutInflater inflater;
    private List<MatchItem> matchItems;
    private ImageLoader imageLoader;

    public LeaguesGamesAdapter(Context context, List<MatchItem> matchItems) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.matchItems = matchItems != null ? matchItems : new ArrayList<MatchItem>();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.flag_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        for (int i = 0; i < this.matchItems.size(); i++) {
            MatchItem item = this.matchItems.get(i);
            MatchItem possibleHeader = new MatchItem();
            possibleHeader.setDate(item.getDate());
            possibleHeader.setSection(true);
            if (!this.matchItems.contains(possibleHeader)) {
                this.matchItems.add(i, possibleHeader);
            }
        }
    }

    public void addMatchItems(List<MatchItem> matchItems) {
        if (matchItems != null) {
            for (MatchItem matchItem : matchItems) {
                if (!this.matchItems.contains(matchItem)) {
                    this.matchItems.add(matchItem);
                }
            }
        }
        for (int i = 0; i < this.matchItems.size(); i++) {
            MatchItem item = this.matchItems.get(i);
            MatchItem possibleHeader = new MatchItem();
            possibleHeader.setDate(item.getDate());
            possibleHeader.setSection(true);
            if (!this.matchItems.contains(possibleHeader)) {
                this.matchItems.add(i, possibleHeader);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return matchItems.size();
    }

    @Override
    public MatchItem getItem(int position) {
        return matchItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isSection() ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        MatchItem item = getItem(position);
        if (item.isSection()) {
            vi = inflater.inflate(R.layout.leagues_games_list_section, parent, false);
            TextView sectionHeader = (TextView) vi.findViewById(R.id.section_title);
            sectionHeader.setText(DateUtils.DATE_FORMAT.format(item.getDate()));
        } else {
            MatchItemHolder holder;
            if (vi == null) {
                vi = inflater.inflate(R.layout.leagues_games_row, parent, false);
                holder = new MatchItemHolder();
                holder.flag1 = (ImageView) vi.findViewById(R.id.flag1);
                holder.team1 = (TextView) vi.findViewById(R.id.team1);
                holder.flag2 = (ImageView) vi.findViewById(R.id.flag2);
                holder.team2 = (TextView) vi.findViewById(R.id.team2);
                holder.middleText = (TextView) vi.findViewById(R.id.middle_text);
                vi.setTag(holder);
            } else {
                holder = (MatchItemHolder) vi.getTag();
            }
            holder.team1.setText(item.getTeam1name());
            holder.team2.setText(item.getTeam2name());
            imageLoader.displayImage(item.getTeam1flagLink(), holder.flag1, options);
            imageLoader.displayImage(item.getTeam2flagLink(), holder.flag2, options);

            holder.middleText.setText(item.getMiddleText());
        }
        return vi;
    }

    public class MatchItemHolder {
        ImageView flag1;
        TextView team1;
        ImageView flag2;
        TextView team2;
        TextView middleText;
    }
}
