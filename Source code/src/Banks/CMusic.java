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

import java.io.IOException;

public class CMusic implements MediaPlayer.OnCompletionListener
{
    public CMusicPlayer mPlayer;
    public short handle = -1;
    public boolean bUninterruptible=false;
    public MediaPlayer mediaPlayer=null;
    public int nLoops;
    public CRunApp application;
    public boolean bPaused=false;
    public int resID;
    public String filename;
    public Uri uri;

    public CMusic(CMusicPlayer m)
    {
    	mPlayer=m;
    	
    }

    public CMusic (CMusicPlayer m, String filename) throws Exception
    {
    	mPlayer=m;
        resID = 0;

        uri = CServices.filenameToURI(filename);

        if (uri == null)
            throw new IOException ("Can't open file");

        mediaPlayer = MediaPlayer.create(MMFRuntime.inst, uri);
        mediaPlayer.setOnCompletionListener(this);
    }

    public void load(short h, CRunApp app) 
    {
        handle=h;
    	application=app;

// STARTCUT
// ENDCUT
        
    	reload();
    }
    public void reload()
    {
        if (uri == null)
        {
            if (resID == 0)
                return;

            mediaPlayer = MediaPlayer.create(MMFRuntime.inst.getApplicationContext(), resID);
        }
        else
        {
            try
            {
                mediaPlayer = MediaPlayer.create(MMFRuntime.inst.getApplicationContext(), uri);
            }
            catch (Throwable t)
            {
            }
        }

        mediaPlayer.setOnCompletionListener(this);
    }
    public void setLoopCount(int count)
    {
    	nLoops=count;
    }
    public void start()
    {
    	if(mediaPlayer != null)
    	{
    		mediaPlayer.start();
    		bPaused=false;
    	}
    }
    public void stop()
    {
    	if(mediaPlayer != null)
    	{
    		mediaPlayer.release();
    		reload();
    	}
    }
    public boolean isPlaying()
    {
    	return mediaPlayer != null && mediaPlayer.isPlaying();
    }
    public void pause()
    {
    	if (mediaPlayer != null && mediaPlayer.isPlaying())
    	{
    		mediaPlayer.pause();
    		bPaused=true;
    	}
    }
    public boolean isPaused()
    {
    	return bPaused;
    }
    public void release()
    {
    	if(mediaPlayer != null)
    	{
    		mediaPlayer.release();
    	}
    }
    public void onCompletion(MediaPlayer mp)
    {
    	mediaPlayer.release();
    	reload();
        if (nLoops>0)
        {
            nLoops--;
            if (nLoops==0)
            {
                mPlayer.removeMusic(this);
                return;
            }
            mediaPlayer.start();
        }
    }
}
