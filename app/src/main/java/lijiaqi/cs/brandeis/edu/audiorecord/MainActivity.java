package lijiaqi.cs.brandeis.edu.audiorecord;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

import static lijiaqi.cs.brandeis.edu.audiorecord.R.id.play_btn;
import static lijiaqi.cs.brandeis.edu.audiorecord.R.id.showHome;
import static lijiaqi.cs.brandeis.edu.audiorecord.R.id.stop_btn;

public class MainActivity extends AppCompatActivity {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    private Thread recordingThread;
    private boolean isRecording = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setButtonHandlers();
        enableButtons(false);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }

    private void setButtonHandlers() {
        ((Button) findViewById(R.id.rec_btn)).setOnClickListener(btnClick);
        ((Button) findViewById(stop_btn)).setOnClickListener(btnClick);
        ((Button) findViewById(play_btn)).setOnClickListener(clickPlay);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.rec_btn, !isRecording);
        enableButton(stop_btn, isRecording);
    }

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                saveAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //convert short to byte
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    private ArrayList<short[]> clips = new ArrayList<>();

    private void saveAudioData() {
        // Write the output audio in byte

        String filePath = "/sdcard/voice8K16bitmono.pcm";
        short sData[] = new short[BufferElements2Rec];

        while (isRecording) {
            // gets the voice output from microphone to byte format

            recorder.read(sData, 0, BufferElements2Rec);
            //System.out.println("Short saving to clips" + sData.toString());
            clips.add(sData);
        }
    }

    private void stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private void playClips(){

        System.out.println(BufferElements2Rec);

        //for(int i = 0; i<clips.size(); i++){

        short sNoise[] = new short[BufferElements2Rec];
        Random random = new Random();
        for(int i=0;i<BufferElements2Rec;i++){
            sNoise[i]=(short) random.nextInt(Short.MAX_VALUE + 1);
        }
        byte[] generatedNoise = short2byte(sNoise);
        AudioTrack at=new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedNoise.length /* 1 second buffer */,
                AudioTrack.MODE_STREAM);
            at.write(generatedNoise, 0, generatedNoise.length);
        System.out.print(generatedNoise.toString());
            at.play();
            //at.release();
        //}
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rec_btn: {
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.stop_btn: {
                    enableButtons(false);
                    stopRecording();
                    break;
                }
            }
        }
    };

    private View.OnClickListener clickPlay = new View.OnClickListener() {
        public void onClick(View v) {
            playClips();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}

