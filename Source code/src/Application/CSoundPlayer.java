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
// CSOUNDPLAYER : synthetiseur MIDI
//
//----------------------------------------------------------------------------------
package Application;

import Banks.*;

import Runtime.Log;
import android.media.AudioManager;
import android.media.SoundPool;

public class CSoundPlayer
{
    public static final int nChannels = 32;
    public CRunApp app;
    boolean bOn = true;
    CSoundChannel channels[];
    public boolean bMultipleSounds = true;
    public SoundPool soundPool = new SoundPool (8, AudioManager.STREAM_MUSIC, 0);

    public float volume = 1.0f;
    public float pan = 1.0f;

    public void setVolume (float volume)
    {
        if (volume > 1.0f)
            volume = 1.0f;

        if (volume < 0.0f)
            volume = 0.0f;

        this.volume = volume;

        for(int i = 0; i < channels.length; ++ i)
            if (channels [i].currentSound != null)
                channels [i].currentSound.updateVolume ();
    }

    public float getVolume ()
    {
        return volume;
    }

    public void setPan (float pan)
    {
        if (pan > 1.0f)
            pan = 1.0f;

        if (pan < 0.0f)
            pan = 0.0f;

        this.pan = pan;

        for(int i = 0; i < channels.length; ++ i)
            if (channels [i].currentSound != null)
                channels [i].currentSound.updateVolume ();
    }

    public float getPan ()
    {
        return pan;
    }

    public CSoundPlayer(CRunApp a)
    {
        app = a;
        channels = new CSoundChannel [nChannels];

        for (int i = 0; i < channels.length; ++ i)
            channels [i] = new CSoundChannel ();
    }

    /** Plays a simple sound.
     */
    public void play(short handle, int nLoops, int channel, boolean bPrio)
    {
        int n;

        if (bOn == false)
        {
            return;
        }

        CSound sound = app.soundBank.newSoundFromHandle(handle);
        if (sound == null)
        {
            return;
        }
        if (bMultipleSounds == false)
        {
            channel = 0;
        }

        // Lance le son
        if (channel < 0)
        {
            for (n = 0; n < nChannels; n++)
                if (channels[n].currentSound == null && !channels[n].locked)
                    break;

            if (n == nChannels)
            {
                // Stoppe le son sur un canal deja en route
                for (n = 0; n < nChannels; n++)
                    if (channels[n].stop (false))
                        break;
            }
            channel = n;
        }

        if (channel < 0 || channel >= nChannels || !channels [channel].stop(false))
            return;

        channels[channel].currentSound = sound;
        sound.setUninterruptible(bPrio);
        sound.setLoopCount(nLoops);
        sound.start();
    }

    public void playFile(String filename, int nLoops, int channel, boolean bPrio)
    {
        int n;

        if (bOn == false)
        {
            return;
        }

        CSound sound;

        try
        {
            sound = new CSound (this, filename);
        }
        catch(Throwable e)
        {
            return;
        }

        if (bMultipleSounds == false)
        {
            channel = 0;
        }

        // Lance le son
        if (channel < 0)
        {
            for (n = 0; n < nChannels; n++)
                if (channels[n].currentSound == null && !channels [n].locked)
                    break;

            if (n == nChannels)
            {
                // Stoppe le son sur un canal deja en route
                for (n = 0; n < nChannels; n++)
                    if (channels[n].stop (false))
                        break;
            }
            channel = n;
        }

        if (channel < 0 || channel >= nChannels || !channels [channel].stop(false))
            return;

        channels[channel].currentSound = sound;
        sound.setUninterruptible(bPrio);
        sound.setLoopCount(nLoops);
        sound.start();
    }

    public void setMultipleSounds(boolean bMultiple)
    {
        bMultipleSounds = bMultiple;
    }

    public void stopAllSounds()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n] != null)
            {
                channels[n].stop(true);
            }
        }
    }

    public void stop(short handle)
    {
        int c;
        for (c = 0; c < nChannels; c++)
        {
            if (channels[c].currentSound != null)
            {
                if (channels[c].currentSound.handle == handle)
                {
                    channels[c].stop(true);
                }
            }
        }
    }

    public boolean isSoundPlaying()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.isPlaying())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSamplePlaying(short handle)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    if (channels[n].currentSound.isPlaying())
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isChannelPlaying(int channel)
    {
        if (channel > 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
                return channels[channel].currentSound.isPlaying();
            }
        }
        return false;
    }

    public boolean isChannelPaused(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
                if (channels[channel].currentSound.isPaused() == true)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public void pause(short handle)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
            		channels[n].currentSound.pause();
                }
            }
        }
    }

    public void resume(short handle)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    channels[n].currentSound.start();
                }
            }
        }
    }

    public void pause()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
        		channels[n].currentSound.pause();
            }
        }
    }

    public void pauseAllChannels()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
        		channels[n].currentSound.pause();
            }
        }
    }

    public void resume()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                channels[n].currentSound.start();
            }
        }
    }

    public void resumeAllChannels()
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                channels[n].currentSound.start();
            }
        }
    }

    public void pauseChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
        		channels[channel].currentSound.pause();
            }
        }
    }

    public void stopChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
            channels[channel].stop(true);
    }

    public void resumeChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
                channels[channel].currentSound.start();
            }
        }
    }

    public int getChannelDuration(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
            	return channels[channel].currentSound.getDuration();
            }
        }
        return 0;
    }

    public void setPositionChannel(int channel, int pos)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
                channels[channel].currentSound.setPosition(pos);
            }
        }
    }

    public int getPositionChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            if (channels[channel].currentSound != null)
            {
                return channels[channel].currentSound.getPosition();
            }
        }
        return 0;
    }

    public void setFrequencyChannel (int channel, int frequency)
    {
        if (channel >= 0 && channel < nChannels)
        {
        	channels[channel].frequency = frequency;
        	
            if (channels[channel].currentSound != null)
            {
                channels[channel].currentSound.frequency = frequency;
                channels[channel].currentSound.updateVolume ();
            }
        }
    }

    public void setVolumeChannel (int channel, float volume)
    {
        if (channel >= 0 && channel < nChannels)
        {
        	channels[channel].volume = volume;
        	
            if (channels[channel].currentSound != null)
            {
                channels[channel].currentSound.volume = volume;
                channels[channel].currentSound.updateVolume ();
            }
        }
    }

    public float getVolumeChannel (int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            return channels[channel].volume;
        }
        return 0;
    }

    public void setPanChannel (int channel, float pan)
    {
        if (channel >= 0 && channel < nChannels)
        {
            channels[channel].pan = pan;
            
            if (channels[channel].currentSound != null)
            {
                channels[channel].currentSound.pan = pan;
                channels[channel].currentSound.updateVolume ();
            }
        }
    }

    public float getPanChannel (int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
        	return channels[channel].pan;
        }
        return 0;
    }

    public void setPosition(short handle, int pos)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    channels[n].currentSound.setPosition(pos);
                }
            }
        }
    }

    public void setVolume(short handle, float volume)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    channels[n].currentSound.volume = volume;
                    channels[n].currentSound.updateVolume ();
                }
            }
        }
    }

    public void setFrequency (short handle, int frequency)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    channels[n].currentSound.frequency = frequency;
                    channels[n].currentSound.updateVolume ();
                }
            }
        }
    }

    public void setPan (short handle, float pan)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound != null)
            {
                if (channels[n].currentSound.handle == handle)
                {
                    channels[n].currentSound.pan = pan;
                    channels[n].currentSound.updateVolume ();
                }
            }
        }
    }

    public void removeSound(CSound sound)
    {
        int n;
        for (n = 0; n < nChannels; n++)
        {
            if (channels[n].currentSound == sound)
            {
                channels[n].stop(true);
            }
        }
    }

    public void lockChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            channels[channel].locked = true;
        }
    }

    public void unlockChannel(int channel)
    {
        if (channel >= 0 && channel < nChannels)
        {
            channels[channel].locked = false;
        }
    }

    public void tick ()
    {
        for (int i = 0; i < nChannels; ++ i)
            if (channels [i].currentSound != null)
                channels [i].currentSound.tick ();
    }
}
