package com.badr.infodota.fragment.hero;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;

import com.badr.infodota.R;
import com.badr.infodota.adapter.HeroResponsesAdapter;
import com.badr.infodota.api.heroes.Hero;
import com.badr.infodota.api.responses.HeroResponse;
import com.badr.infodota.api.responses.HeroResponsesResult;
import com.badr.infodota.util.FileUtils;
import com.badr.infodota.util.LoaderProgressTask;
import com.badr.infodota.util.ProgressTask;
import com.google.gson.Gson;

import java.io.File;
import java.util.List;

/**
 * User: ABadretdinov
 * Date: 05.02.14
 * Time: 17:53
 */
public class HeroResponses extends Fragment {
    private MediaPlayer mediaPlayer;
    private HeroResponsesAdapter adapter;
    private Filter mFilter;
    private EditText searchView;
    private ListView listView;
    private Hero hero;

    public static HeroResponses newInstance(Hero hero) {
        HeroResponses fragment = new HeroResponses();
        fragment.hero = hero;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.hero_responses, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View root = getView();
        if (root == null) {
            return;
        }
        listView = (ListView) root.findViewById(android.R.id.list);
        searchView = (EditText) root.findViewById(R.id.search);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mFilter != null) {
                    mFilter.filter(s);
                }
            }
        });
        root.findViewById(R.id.select_to_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.changeEditMode(true);
                root.findViewById(R.id.buttons_holder).setVisibility(View.VISIBLE);
            }
        });
        root.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.changeEditMode(false);
                root.findViewById(R.id.buttons_holder).setVisibility(View.GONE);
            }
        });
        root.findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.startLoadingFiles();
                root.findViewById(R.id.buttons_holder).setVisibility(View.GONE);
            }
        });
        root.findViewById(R.id.invert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.inverseChecked();
            }
        });
        root.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });
        final Activity activity = getActivity();
        if (activity != null) {
            new LoaderProgressTask<Pair<List<HeroResponse>, String>>(new ProgressTask<Pair<List<HeroResponse>, String>>() {
                @Override
                public Pair<List<HeroResponse>, String> doTask(OnPublishProgressListener listener) throws Exception {
                    String responsesEntity = FileUtils.getTextFromAsset(getActivity(),
                            "heroes" + File.separator + hero.getDotaId() + File.separator + "responses.json");
                    HeroResponsesResult heroResponsesResult = new Gson().fromJson(responsesEntity, HeroResponsesResult.class);

                    List<HeroResponse> heroResponses = heroResponsesResult.getResponses();
                    File musicFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "Music" + File.separator + "dota2" + File.separator + hero.getDotaId() + File.separator);
                    if (musicFolder.exists()) {
                        for (HeroResponse heroResponse : heroResponses) {
                            String[] urlParts = heroResponse.getUrl().split(File.separator);
                            String fileName = musicFolder + File.separator + urlParts[urlParts.length - 1];
                            if (new File(fileName).exists()) {
                                heroResponse.setLocalUrl(fileName);
                            }
                        }
                    }
                    return Pair.create(heroResponses, musicFolder.getAbsolutePath());
                }

                @Override
                public void doAfterTask(Pair<List<HeroResponse>, String> result) {
                    adapter = new HeroResponsesAdapter(getActivity(), result.first, result.second);
                    mFilter = adapter.getFilter();
                    listView.setAdapter(adapter);
                }

                @Override
                public void handleError(String error) {

                }

                @Override
                public String getName() {
                    return null;
                }
            }, null).execute();
        }
        setOnClickListener();
    }

    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setOnClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (adapter.isEditMode()) {
                    adapter.setItemClicked(position);
                } else {
                    HeroResponse heroResponse = adapter.getItem(position);
                    new MusicLoader(position).execute(heroResponse);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        killMediaPlayer();
    }

    public class MusicLoader extends AsyncTask<HeroResponse, Object, String> {
        private int position;

        public MusicLoader(int position) {
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            adapter.addToPlayLoading(position);
        }

        @Override
        protected String doInBackground(HeroResponse... params) {
            HeroResponse heroResponse = params[0];
            String path = heroResponse.getUrl();
            if (!TextUtils.isEmpty(heroResponse.getLocalUrl())) {
                File filePath = new File(heroResponse.getLocalUrl());
                if (filePath.exists()) {
                    path = heroResponse.getLocalUrl();
                }
            }
            try { //http://developer.android.com/intl/ru/reference/android/media/AsyncPlayer.html
                killMediaPlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
                mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.loaded(position);
            Context context = getActivity();
            if (!TextUtils.isEmpty(s) && context != null) {
                Toast.makeText(context, getString(R.string.loading_response_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
