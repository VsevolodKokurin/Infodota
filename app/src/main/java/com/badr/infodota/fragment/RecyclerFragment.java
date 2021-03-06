package com.badr.infodota.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badr.infodota.R;
import com.badr.infodota.adapter.BaseRecyclerAdapter;
import com.badr.infodota.adapter.OnItemClickListener;
import com.badr.infodota.adapter.holder.BaseViewHolder;

/**
 * Created by Badr on 21.12.2014.
 */
public abstract class RecyclerFragment<T, VIEW_HOLDER extends BaseViewHolder> extends Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    protected SwipeRefreshLayout mListContainer;
    protected RecyclerView mRecyclerView;
    protected View mProgressBar;
    protected int layoutId = R.layout.recycler_content;
    protected BaseRecyclerAdapter<T, VIEW_HOLDER> mAdapter;

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public RecyclerView getRecyclerView() {
        ensureList();
        return mRecyclerView;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    private void ensureList() {
        if (mRecyclerView != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof RecyclerView) {
            mRecyclerView = (RecyclerView) root;
        } else {
            mRecyclerView = (RecyclerView) root.findViewById(android.R.id.list);
        }
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(getLayoutManager());
        mListContainer = (SwipeRefreshLayout) root.findViewById(R.id.listContainer);
        if (mListContainer != null) {
            mListContainer.setColorSchemeResources(R.color.primary);
            mListContainer.setOnRefreshListener(this);

        }
        mProgressBar = root.findViewById(R.id.progressBar);
        if (mAdapter != null) {
            setAdapter(mAdapter);
        }
    }

    public BaseRecyclerAdapter<T, VIEW_HOLDER> getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseRecyclerAdapter<T, VIEW_HOLDER> adapter) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        mAdapter = adapter;
        if (mRecyclerView != null) {
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recycler_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    public void setRefreshing(boolean refreshing) {
        mListContainer.setRefreshing(refreshing);
    }
}
