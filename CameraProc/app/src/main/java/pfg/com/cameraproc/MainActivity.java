package pfg.com.cameraproc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pfg.com.cameraproc.camera2basic.Camera2BasicFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }
    }
}
