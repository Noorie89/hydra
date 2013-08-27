/**
 *
 * @author Tom Naessens Tom.Naessens@UGent.be 3de Bachelor Informatica
 * Universiteit Gent
 * @author Stijn Seghers <stijn.seghers at ugent.be>
 */
package be.ugent.zeus.hydra;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import be.ugent.zeus.hydra.data.Song;
import be.ugent.zeus.hydra.data.caches.SongCache;
import be.ugent.zeus.hydra.data.services.HTTPIntentService;
import be.ugent.zeus.hydra.data.services.UrgentService;
import be.ugent.zeus.hydra.util.audiostream.MusicService;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;

public class Urgent extends AbstractSherlockActivity {

    private static final String TAG = "Urgent";
    private static final String DEFAULT_RESPONSE = "Geen plaat(info)";
    private static final int REFRESH_TIME = 20 * 1000;
    private final Handler handler = new Handler();
    private Runnable refresh;
    private SongCache cache;
    private TextView show;
    private TextView previous;
    private TextView current;
    LinearLayout prevContainer;
    LinearLayout currentContainer;
    private ShareActionProvider mShareActionProvider;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setTitle(R.string.title_urgent);
        setContentView(R.layout.urgent);

        final ImageButton btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        if (MusicService.mState == MusicService.State.Playing) {
            btnPlay.setImageResource(R.drawable.button_urgent_pause);
        } else {
            btnPlay.setImageResource(R.drawable.button_urgent_play);
        }


        show = (TextView) findViewById(R.id.urgent_current_show);
        previous = (TextView) findViewById(R.id.urgent_vorige);
        current = (TextView) findViewById(R.id.urgent_huidige);

        prevContainer = (LinearLayout) findViewById(R.id.urgent_vorige_container);
        currentContainer = (LinearLayout) findViewById(R.id.urgent_huidige_container);

        btnPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (MusicService.mState != MusicService.State.Playing) {
                    btnPlay.setImageResource(R.drawable.button_urgent_pause);

                    handler.post(refresh);

                } else {
                    btnPlay.setImageResource(R.drawable.button_urgent_play);

                    handler.removeCallbacks(refresh);
                }
                startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
            }
        });

        refresh = new Runnable() {
            public void run() {
                Log.d(TAG, "refreshing time!");
                refresh();
                handler.postDelayed(refresh, REFRESH_TIME);
            }
        };

        cache = SongCache.getInstance(Urgent.this);

        Log.v(TAG, "opgestart");


        ImageView home = (ImageView) findViewById(R.id.urgent_share_home);
        ImageView facebook = (ImageView) findViewById(R.id.urgent_share_facebook);
        ImageView twitter = (ImageView) findViewById(R.id.urgent_share_twitter);
        ImageView soundcloud = (ImageView) findViewById(R.id.urgent_share_soundcloud);
        ImageView mail = (ImageView) findViewById(R.id.urgent_share_mail);

        final String homeUrl = "http://urgent.fm";
        final String facebookUrl = "https://www.facebook.com/pages/Urgentfm/28367168655";
        final String twitterUrl = "https://mobile.twitter.com/urgentfm";
        final String soundcloudUrl = "http://m.soundcloud.com/urgent-fm-official";

        home.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openUrl(homeUrl);
            }
        });

        facebook.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openUrl(facebookUrl);
            }
        });

        twitter.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openUrl(twitterUrl);
            }
        });

        soundcloud.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                openUrl(soundcloudUrl);
            }
        });

        mail.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendMail();
            }
        });

    }

    public void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void sendMail() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@urgent.fm"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Bericht via Hydra");
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(Urgent.this, "Geen emailclient geinstalleerd.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refresh);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cache.clear();
        if (MusicService.mState == MusicService.State.Playing) {
            handler.post(refresh);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share, menu);
        MenuItem menuItem = menu.findItem(R.id.share);
        mShareActionProvider = new ShareActionProvider(getSupportActionBar().getThemedContext());
        menuItem.setActionProvider(mShareActionProvider);

        updateShareIntent();

        return super.onCreateOptionsMenu(menu);
    }

    private void refresh() {
        Intent intent = new Intent(this, UrgentService.class);
        intent.putExtra(HTTPIntentService.RESULT_RECEIVER_EXTRA, new SongResultReceiver());

        startService(intent);
    }

    private boolean is_info_valid(String info) {
        return info != null && !DEFAULT_RESPONSE.equals(info);
    }

    private void updateShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        Song currentSong = cache.get(SongCache.CURRENT);
        String text = getResources().getString(R.string.urgent_share_without_song);
        Log.d(TAG, currentSong + "");
        if (currentSong != null) {
            Log.d(TAG, currentSong.title_and_artist);
            Log.d(TAG, currentSong.program);
            if (is_info_valid(currentSong.title_and_artist)) {
                if (is_info_valid(currentSong.program)) {
                    text = getResources().getString(R.string.urgent_share_song_show, currentSong.title_and_artist, currentSong.program);
                } else {
                    text = getResources().getString(R.string.urgent_share_song, currentSong.title_and_artist);
                }
            } else if (is_info_valid(currentSong.program)) {
                text = getResources().getString(R.string.urgent_share_show, currentSong.program);
            }
        }
        Log.d(TAG, text);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.urgent_share_subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        mShareActionProvider.setShareIntent(shareIntent);
    }

    private class SongResultReceiver extends ResultReceiver {

        SongResultReceiver() {
            super(null);
        }

        @Override
        public void onReceiveResult(int code, Bundle icicle) {
            switch (code) {
                case HTTPIntentService.STATUS_FINISHED:
                    Urgent.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Song curSong = cache.get(SongCache.CURRENT);
                            Song prevSong = cache.get(SongCache.PREVIOUS);

                            if (prevSong != null && !DEFAULT_RESPONSE.equals(prevSong.title_and_artist)) {
                                previous.setText(prevSong.title_and_artist.toUpperCase());
                                prevContainer.setVisibility(TextView.VISIBLE);

                                Log.v(TAG, "Previous: " + prevSong.title_and_artist + " (" + prevSong.program + ")");
                            } else {
                                prevContainer.setVisibility(TextView.GONE);

                                Log.v(TAG, "Previous: " + R.string.no_song_info_found);
                            }
                            if (curSong != null && !DEFAULT_RESPONSE.equals(curSong.title_and_artist)) {
                                current.setText(curSong.title_and_artist.toUpperCase());
                                currentContainer.setVisibility(TextView.VISIBLE);

                                Log.v(TAG, "Current: " + curSong.title_and_artist + " (" + curSong.program + ")");
                            } else {
                                currentContainer.setVisibility(TextView.GONE);

                                Log.v(TAG, "Current: " + R.string.no_song_info_found);
                            }

                            if (curSong != null && !show.getText().equals(String.format(getResources().getString(R.string.urgent_show_prefix),
                                    curSong.program.toUpperCase()))) {
                                show.setText(
                                        String.format(getResources().getString(R.string.urgent_show_prefix),
                                        curSong.program.toUpperCase()));
                            }
                            updateShareIntent();
                        }
                    });
                    break;
                case HTTPIntentService.STATUS_ERROR:
                    Toast.makeText(Urgent.this, R.string.nowplaying_update_failed, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
