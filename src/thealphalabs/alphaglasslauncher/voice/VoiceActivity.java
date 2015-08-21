package thealphalabs.alphaglasslauncher.voice;

import android.app.Activity;
import android.os.Bundle;

import thealphalabs.alphaglasslauncher.flip.FlipViewController;
import thealphalabs.alphaglasslauncher.util.AppInfoProvider;

public class VoiceActivity extends Activity {

    private FlipViewController flipView;
    private AppInfoProvider appInfoProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appInfoProvider= AppInfoProvider.getInstance(this);

        flipView = new FlipViewController(this, FlipViewController.HORIZONTAL);

        flipView.setAdapter(new UIAdapter(this,appInfoProvider));

        setContentView(flipView);

    }
}
