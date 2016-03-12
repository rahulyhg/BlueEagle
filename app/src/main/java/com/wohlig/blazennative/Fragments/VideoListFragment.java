package com.wohlig.blazennative.Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wohlig.blazennative.Activities.MainActivity;
import com.wohlig.blazennative.Adapters.VideoListAdapter;
import com.wohlig.blazennative.HttpCall.HttpCall;
import com.wohlig.blazennative.POJOs.VideoListPojo;
import com.wohlig.blazennative.R;
import com.wohlig.blazennative.Util.InternetOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoListFragment extends Fragment {
    private View view;
    private static Activity activity;
    private static String TAG = "BLAZEN";
    private static ProgressBar progressBar;
    private RecyclerView rvVideoList;
    private List<VideoListPojo> listVideos;
    private VideoListAdapter videoListAdapter;
    private String id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_video_list, container, false);

        ((MainActivity) this.getActivity()).setToolbarText("VIDEO");
        id = ((MainActivity) this.getActivity()).getId();

        activity = getActivity();

        initilizeViews();

        return view;
    }

    private void initilizeViews() {

        rvVideoList = (RecyclerView) view.findViewById(R.id.rvVideoList);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        listVideos = new ArrayList<VideoListPojo>();

        videoListAdapter = new VideoListAdapter(listVideos);
        rvVideoList.setAdapter(videoListAdapter);

        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvVideoList.setLayoutManager(llm);

        //rvVideoAlbum.addItemDecoration(new SpacesItemDecoration(Size.dpToPx(activity, 10)));

        getContent();
    }

    private void getContent() {

        new AsyncTask<Void, Void, String>() {
            boolean done = false;
            boolean noInternet = false;

            @Override
            protected String doInBackground(Void... params) {

                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                String response;

                JSONArray jsonArray;

                try {
                    response = HttpCall.getDataGet(InternetOperations.SERVER_URL + "video/getAlbumVideos?id=" + id);
                    if (!response.equals("")) {                 //check is the response empty
                        jsonArray = new JSONArray(response);

                        if (jsonArray.length() > 0) {

                            for (int i = 0; i < jsonArray.length(); i++) {

                                String videoId = null, link = null, title = null;

                                JSONObject jsonObject = new JSONObject(jsonArray.get(i).toString());
                                videoId = jsonObject.optString("id");
                                title = jsonObject.optString("title");

                                populateVideoList(videoId, link, title);
                            }
                            done = true;

                        } else {
                            done = true;
                        }
                    } else {                                    //no internet and no cached copy also found in database
                        noInternet = true;
                    }

                } catch (JSONException je) {
                    Log.e(TAG, Log.getStackTraceString(je));
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                progressBar.setVisibility(View.GONE);
                if (done) {                         //everything went fine
                    refresh();
                } else if (noInternet) {            //if no internet and no cached copy found in database
                    Toast.makeText(activity, "No internet!", Toast.LENGTH_SHORT).show();
                } else {                            //some error
                    Toast.makeText(activity, "Oops, Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(null, null, null);
    }

    public void populateVideoList(String videoId, String link, String title) {

        VideoListPojo vap = new VideoListPojo();
        vap.setVideoId(videoId);
        vap.setTitle(title);
        vap.setContext(activity);
        listVideos.add(vap);
    }

    private void refresh() {
        videoListAdapter.notifyDataSetChanged();
    }


}