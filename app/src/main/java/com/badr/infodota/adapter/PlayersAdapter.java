package com.badr.infodota.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.badr.infodota.BeanContainer;
import com.badr.infodota.R;
import com.badr.infodota.activity.BaseActivity;
import com.badr.infodota.adapter.holder.PlayerHolder;
import com.badr.infodota.adapter.pager.MatchHistoryPagerAdapter;
import com.badr.infodota.api.dotabuff.Unit;
import com.badr.infodota.service.player.PlayerService;
import com.badr.infodota.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Histler
 * Date: 21.01.14
 */
public class PlayersAdapter extends BaseRecyclerAdapter<Unit, PlayerHolder> implements Filterable {
    protected ImageLoader imageLoader;
    DisplayImageOptions options;
    PlayerService playerService = BeanContainer.getInstance().getPlayerService();
    private List<Unit> filtered;
    private String[] groupNames;
    private boolean showGroup = true;

    public PlayersAdapter(List<Unit> players, boolean groupVisible, String[] groupNames) {
        super(players);
        this.filtered = mData;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.steam_default)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader = ImageLoader.getInstance();
        this.groupNames = groupNames;
        showGroup = groupVisible;
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }

    @Override
    public Unit getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public PlayerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_row, parent, false);
        return new PlayerHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(PlayerHolder holder, int position) {
        final Unit unit = getItem(position);
        holder.name.setText(unit.getName());
        imageLoader.displayImage(unit.getIcon(), holder.icon, options);
        if (unit.getGroup() == Unit.Groups.NONE) {

            addInit(holder, unit);
        } else {
            deleteInit(holder, unit);
        }
    }

    private void deleteInit(final PlayerHolder holder, final Unit unit) {
        holder.add.setVisibility(View.GONE);
        if (showGroup) {
            holder.group.setVisibility(View.VISIBLE);
            holder.group.setText(groupNames[unit.getGroup().ordinal()]);
        } else {
            holder.group.setVisibility(View.GONE);
        }
        holder.delete.setVisibility(View.VISIBLE);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                Utils.deletePlayerFromListDialog(context, unit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playerService.deleteAccount(context, unit);
                        addInit(holder, unit);
                        ViewPager viewPager = (ViewPager) ((BaseActivity) context).findViewById(R.id.pager);
                        if (viewPager != null && viewPager.getAdapter() instanceof MatchHistoryPagerAdapter) {
                            MatchHistoryPagerAdapter adapter = (MatchHistoryPagerAdapter) viewPager.getAdapter();
                            if (adapter != null) {
                                adapter.update();
                            }
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void addInit(final PlayerHolder holder, final Unit unit) {

        holder.add.setVisibility(View.VISIBLE);
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();
                Utils.addPlayerToListDialog(context, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            unit.setGroup(Unit.Groups.FRIEND);
                        } else {
                            unit.setGroup(Unit.Groups.PRO);
                        }
                        playerService.saveAccount(context, unit);
                        deleteInit(holder, unit);
                        ViewPager viewPager = (ViewPager) ((BaseActivity) context).findViewById(R.id.pager);
                        if (viewPager != null && viewPager.getAdapter() instanceof MatchHistoryPagerAdapter) {
                            MatchHistoryPagerAdapter adapter = (MatchHistoryPagerAdapter) viewPager.getAdapter();
                            adapter.update();
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
        holder.group.setVisibility(View.GONE);
        holder.delete.setVisibility(View.GONE);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                List<Unit> filteredUnits = new ArrayList<Unit>();
                if (constraint == null) {
                    filterResults.values = mData;
                    filterResults.count = mData.size();
                    return filterResults;
                }
                String lowerConstr = constraint.toString().toLowerCase();
                for (Unit unit : mData) {
                    if ((unit.getLocalName() != null && unit.getLocalName().toLowerCase().contains(lowerConstr)) || (unit.getName() != null && unit.getName().toLowerCase().contains(lowerConstr))) {
                        filteredUnits.add(unit);
                    }
                }
                filterResults.count = filteredUnits.size();
                filterResults.values = filteredUnits;
                return filterResults;
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (ArrayList<Unit>) results.values;
                if (filtered == null) {
                    filtered = new ArrayList<Unit>();
                }
                notifyDataSetChanged();
            }
        };
    }
}
