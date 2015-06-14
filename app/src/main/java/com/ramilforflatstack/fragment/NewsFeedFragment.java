package com.ramilforflatstack.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.ramilforflatstack.R;
import com.ramilforflatstack.adapter.NewsFeedItemAdapter;
import com.ramilforflatstack.content.NewsFeedItem;
import com.ramilforflatstack.response.NewsFeedResponse;
import com.ramilforflatstack.response.NewsFeedResponseContent;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * Created by Ramil on 14.06.2015.
 */
public class NewsFeedFragment extends Fragment implements SwipyRefreshLayout.OnRefreshListener {

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @InjectView(R.id.swipyrefreshlayout)
    SwipyRefreshLayout mSwipeLayout;

    private Gson mGson = new Gson();

    private NewsFeedItemAdapter mNewsFeedItemAdapter;
    List<NewsFeedItem> items = new ArrayList<>();

    private VKRequest.VKRequestListener mListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            super.onComplete(response);
            NewsFeedResponse resp = mGson.fromJson( response.responseString, NewsFeedResponse.class );
            NewsFeedResponseContent content = resp.content;
            List<NewsFeedItem> newItem = content.toNewsList();
            items.clear();
            for (int i = 0; i < newItem.size(); i++) {
                items.add(newItem.get(i));
            }

            mNewsFeedItemAdapter.notifyDataSetChanged();
            mSwipeLayout.setRefreshing(false);
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            super.attemptFailed(request, attemptNumber, totalAttempts);
        }

        @Override
        public void onError(VKError error) {
            super.onError(error);
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
            super.onProgress(progressType, bytesLoaded, bytesTotal);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fmt_news_feed, container, false);
        ButterKnife.inject(this, view);

        return  view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mNewsFeedItemAdapter = new NewsFeedItemAdapter(items, getActivity());

        mSwipeLayout.setRefreshing(true);
        VKRequest request = new VKRequest("newsfeed.get", VKParameters.from("filters", "post"));
        request.executeWithListener(mListener);

        mRecyclerView.setAdapter(mNewsFeedItemAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onResume(){
        super.onResume();
        mSwipeLayout.setOnRefreshListener(this);
    }

    @Override
    public void onPause(){
        mSwipeLayout.setOnRefreshListener(null);
        super.onPause();
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection direction) {
        Log.d("mytag", "Refresh triggered at "
                + (direction == SwipyRefreshLayoutDirection.TOP ? "top" : "bottom"));

        VKRequest request = new VKRequest("newsfeed.get", VKParameters.from("filters", "post"));
        request.executeWithListener(mListener);
    }
}
