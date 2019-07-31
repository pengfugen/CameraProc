package pfg.com.cameraproc;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

public class VideoEncoder {

    private static VideoEncoder mEncoder;

    MediaCodec mCodec;
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int BIT_RATE = 2072576;            //
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    Surface mInputSurface;
    private MediaCodec.BufferInfo mBufferInfo;
    File mfile;
    FileOutputStream osw;

    String TAG = "VideoEncoder";

    public static VideoEncoder getInstance() {
        if(mEncoder == null)
            mEncoder = new VideoEncoder();
        return mEncoder;
    }

    public void initCodec(int width, int height) {
        mBufferInfo = new MediaCodec.BufferInfo();
        mfile = new File(Environment.getExternalStorageDirectory()+"/video.264");

        try {
            mfile.createNewFile();
            osw = new FileOutputStream(mfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogTool.loge(TAG, "File not found");
        }
        catch (IOException e) {
            e.printStackTrace();
            LogTool.loge(TAG, "Create new File failed");
        }
        try {
            mCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            //format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            mCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            // 编码需要Input Surface，解码configure中设置output surface
            mInputSurface = mCodec.createInputSurface();
            mCodec.setCallback(new EncoderCallback());
            mCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Surface getInputSurface() {
        return mInputSurface;
    }

    public void startEncode() {
        //mCodec.start();
    }

    public void release() {
        try {
            osw.flush();
            osw.close();
        } catch (IOException e) {
            LogTool.loge(TAG, "Close file error:"+e.getMessage());
        }

        if (mCodec != null) {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
    }

    private class EncoderCallback extends MediaCodec.Callback{
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {

        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {

            //编码后的数据
            LogTool.logd(TAG, "onOutputBufferAvailable, index="+index+" info.size=" + info.size);
            ByteBuffer outPutByteBuffer = mCodec.getOutputBuffer(index);
            byte[] outData = new byte[info.size];
            outPutByteBuffer.get(outData);

            //开始RTSP传输
            try {
                osw.write(outData);
            } catch (IOException e) {
                LogTool.loge(TAG, "Write file error:"+e.getMessage());
            }

            mCodec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(MediaCodec codec, MediaCodec.CodecException e) {
            LogTool.loge(TAG, "Error: " + e);
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
            LogTool.logd(TAG, "encoder output format changed: " + format);
        }
    }
}
