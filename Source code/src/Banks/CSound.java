/* Copyright (c) 1996-2013 Clickteam
 *
 * This source code is part of the Android exporter for Clickteam Multimedia Fusion 2.
 * 
 * Permission is hereby granted to any person obtaining a legal copy 
 * of Clickteam Multimedia Fusion 2 to use or modify this source code for 
 * debugging, optimizing, or customizing applications created with 
 * Clickteam Multimedia Fusion 2.  Any other use of this source code is prohibited.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */
//----------------------------------------------------------------------------------
//
// CSOUND : un echantillon
//
//----------------------------------------------------------------------------------
package Banks;

import android.media.*;
import android.net.Uri;
import Application.*;
import Services.CServices;
import Runtime.*;

public class CSound implements MediaPlayer.OnCompletionListener
{
    public CSoundPlayer sPlayer;
    public short handle = -1;
    public boolean bUninterruptible=false;
    public MediaPlayer mediaPlayer=null;
    int nLoops = 1;
    public CRunApp application;
    public int length;

    public int soundID = -1;
    public int streamID = -1;

    public long streamStart;
    public long streamDuration;
    public float streamRate;

    public int resID;
    Uri uri;

    boolean bPaused;

    public float volume = 1.0f;
    public float pan = 1.0f;

    public int origFrequency = -1;
    public int frequency = -1;

    public float [] getVolumes ()
    {
        float volume = (sPlayer.volume * this.volume);
        float pan = (sPlayer.pan * this.pan);

        float leftMod = 1.0f, rightMod = 1.0f;

        if (pan > 1.0f)
            leftMod = 1.0f - (pan - 1.0f);

        if (pan < 1.0f)
            rightMod = 1.0f - (1.0f - pan);

        float volumes [] = new float [2];

        volumes [0] = volume * leftMod;
        volumes [1] = volume * rightMod;

        return volumes;
    }

    public float getRate ()
    {
        if (frequency == -1)
            return 1.0f;

        return ((float) frequency) / ((float) origFrequency);
    }

    public void updateVolume ()
    {
        float [] volumes = getVolumes ();

        if (soundID == -1)
        {
            if (mediaPlayer != null)
                mediaPlayer.setVolume (volumes [0], volumes [1]);
        }
        else
        {
            if (streamID != -1)
            {
                sPlayer.soundPool.setVolume (streamID, volumes [0], volumes [1]);

                float newRate = getRate ();

                streamDuration =
                        (long) ((streamDuration * ((1.0f - streamRate) + 1.0f)) * ((1.0f - newRate) + 1.0f));

                sPlayer.soundPool.setRate (streamID, newRate);

                streamRate = newRate;
            }
        }
    }

    public CSound(CSoundPlayer p, short handle, int soundID, int frequency, int length)
    {
        sPlayer=p;

        this.soundID = soundID;

        this.length = length;

        this.frequency = frequency;
        this.origFrequency = frequency;

        this.handle = handle;

    }

    public CSound (CSound sound)
    {
        sPlayer = sound.sPlayer;

        resID = sound.resID;
        uri = sound.uri;

        soundID = sound.soundID;

        frequency = sound.frequency;
        origFrequency = sound.origFrequency;
        length = sound.length;
        handle = sound.handle;

        createPlayer ();
    }

    public CSound(CSoundPlayer p, String filename)
    {
        sPlayer=p;

        if ((uri = CServices.filenameToURI(filename)) == null)
            throw new RuntimeException ("Can't open file");

        createPlayer ();
    }

    public void load(short h, CRunApp app)
    {
        handle = h;
    	application=app;

        resID = MMFRuntime.inst.getResourceID(String.format ("raw/s%04d", h));
    }

    private void createPlayer()
    {
        if (soundID == -1)
        {
            try
            {
                if (uri != null)
                    mediaPlayer = MediaPlayer.create(MMFRuntime.inst.getApplicationContext(), uri);
                else
                    mediaPlayer = MediaPlayer.create(MMFRuntime.inst.getApplicationContext(), resID);

                mediaPlayer.setOnCompletionListener(this);
            }
            catch(RuntimeException e)
            {
                Log.Log("Error loading sound: " + e);
                mediaPlayer = null;
            }
        }

        updateVolume ();

        bPaused = false;
    }

    public void setLoopCount(int count)
    {
        if (count <= 0)
            nLoops = 0;
        else
    	    nLoops = count;
    }
    public void setUninterruptible(boolean bFlag)
    {
    	bUninterruptible=bFlag;
    }
    public void start()
    {
        bPaused = false;

        if (soundID == -1)
        {
            if (mediaPlayer != null)
            {
                mediaPlayer.setLooping (nLoops == 0);
                mediaPlayer.start();
            }
        }
        else
        {
            float [] volumes = getVolumes ();
            streamRate = getRate ();

            streamDuration = (long) Math.round (((1.0f - streamRate) + 1.0f) * ((float) ((length * nLoops) + 150)));

            streamID = sPlayer.soundPool.play
                    (soundID, volumes [0], volumes [1], 0, nLoops <= 0 ? -1 : nLoops - 1, streamRate);

            streamStart = (nLoops == 0 ? -1 : System.currentTimeMillis ());
        }

    }
    public void stop()
    {
        if (soundID == -1)
        {
            if(mediaPlayer != null)
            {
                mediaPlayer.release();
                mediaPlayer = null;

                createPlayer();
            }
        }
        else
        {
            if (streamID != -1)
            {
                sPlayer.soundPool.stop (streamID);
                streamID = -1;
            }
        }
    }

    public boolean isPlaying()
    {
    	if (soundID == -1)
        {
            try
            {
                return mediaPlayer != null && mediaPlayer.isPlaying();
            }
            catch (IllegalStateException e)
            {
                return false;
            }
        }
        else
        {
            return streamID != -1;
        }
    }

    public boolean isPaused()
    {
    	return bPaused;
    }
    public void pause()
    {
        if (soundID == -1)
        {
            if (mediaPlayer != null && mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                bPaused = true;
            }
        }
        else
        {
            if (streamID != -1)
                sPlayer.soundPool.pause (streamID);
        }
    }

    public int getDuration()
    {
        if (soundID == -1)
        {
            if (mediaPlayer == null)
                return 0;

            return mediaPlayer.getDuration();
        }
        else
        {
            return length;
        }
    }
    public void setPosition(int position)
    {
        if (soundID == -1)
        {
            try
            {
                if (mediaPlayer != null)
                    mediaPlayer.seekTo(position);
            }
            catch (IllegalStateException e)
            {
            }
        }
    }
    public int getPosition()
    {
        if (soundID == -1)
        {
            try
            {
                if (mediaPlayer != null)
                    mediaPlayer.getCurrentPosition();
            }
            catch (IllegalStateException e)
            {
            }
        }

        return 0;
    }

    public void release()
    {
		if(mediaPlayer == null)
    		return;

    	mediaPlayer.release();
    }

    public void onCompletion(MediaPlayer mp)
    {
        if (nLoops != 0 && (-- nLoops) == 0)
        {
            sPlayer.removeSound(this);
            return;
        }

        mediaPlayer.start();
    }

    public void tick ()
    {
        if (streamID != -1 && streamStart != -1 &&
                    System.currentTimeMillis() > (streamStart + streamDuration))
        {
            sPlayer.removeSound(this);
        }
    }
}
