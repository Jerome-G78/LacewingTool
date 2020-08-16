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
// CMUSICBANK : Stockage des musiques
//
//----------------------------------------------------------------------------------
package Banks;

import Services.*;
import Application.*;
import Runtime.*;

import android.media.AudioManager;
import android.media.SoundPool;

public class CSoundBank implements IEnum
{
    public CSound sounds[] = null;
    public int nHandlesReel;
    public int nHandlesTotal;
    public int nSounds;
    public int handleToLength [];
    public int handleToFrequency [];
    public int handleToSoundID [];
    public short handleToFlags [];
    short useCount[];
    public CSoundPlayer sPlayer;

    public CSoundBank(CSoundPlayer p)
    {
        sPlayer=p;
    }

    public void preLoad(CFile f)
    {
        // Nombre de handles
        nHandlesReel = f.readAShort();

        Log.Log ("nHandlesReel: " + nHandlesReel);

        handleToLength = new int [nHandlesReel];
        handleToFrequency = new int [nHandlesReel];
        handleToFlags = new short [nHandlesReel];
        handleToSoundID = new int [nHandlesReel];

        int numberOfSounds = f.readAShort ();

        Log.Log ("numberOfSounds: " + numberOfSounds);

        for (int i = 0; i < numberOfSounds; ++ i)
        {
            int handle = f.readAShort ();

            handleToFlags[handle] = f.readAShort ();
            handleToLength [handle] = f.readAInt ();
            handleToFrequency [handle] = f.readAInt ();
            handleToSoundID [handle] = -1;
        }

        useCount = new short[nHandlesReel];
        resetToLoad();
        nSounds = 0;
        sounds = null;
    }

    public CSound newSoundFromHandle(short handle)
    {
        if (sounds != null)
        {
            if (handle >= 0 && handle < nHandlesReel)
            {
                if (sounds[handle] != null)
                {
                    return new CSound (sounds[handle]);
                }
            }
        }
        return null;
    }

    public void resetToLoad()
    {
        int n;
        for (n = 0; n < nHandlesReel; n++)
        {
            useCount[n] = 0;
        }
    }

    public void setToLoad(short handle)
    {
        useCount[handle]++;
    }

    // Entree enumeration
    public short enumerate(short num)
    {
        setToLoad(num);
        return -1;
    }

    public void load(CRunApp app)
    {
        int h;

        // Combien de musiques?
        nSounds = 0;
        for (h = 0; h < nHandlesReel; h++)
        {
            if (useCount[h] != 0)
            {
                nSounds++;
            }
            else
            {
                if (handleToSoundID[h] != -1)
                {
                    sPlayer.soundPool.unload (handleToSoundID[h]);
                    handleToSoundID[h] = -1;
                }

                if (sounds != null && sounds[h]!=null)
                    sounds[h].release();
            }
        }

        // Charge les images
        CSound newSounds[] = new CSound[nHandlesReel];

        for (h = 0; h < nHandlesReel; h++)
        {
            if (useCount[h] != 0)
            {
                if (sounds != null && sounds[h] != null)
                {
                    newSounds[h] = sounds[h];
                }
                else
                {
                    if (handleToSoundID[h] == -1 && (handleToFlags[h] & 0x20) == 0) /* play from disk not set? */
                    {
                        int resID = MMFRuntime.inst.getResourceID(String.format("raw/s%04d", h));

                        try
                        {
                            handleToSoundID[h] = sPlayer.soundPool.load
                                    (MMFRuntime.inst.getApplicationContext(), resID, 0);
                        }
                        catch (Throwable t)
                        {
                        }
                    }

                    newSounds[h] = new CSound
                            (sPlayer, (short) h, handleToSoundID[h], handleToFrequency[h], handleToLength[h]);

                    if (handleToSoundID[h] == -1)
                        newSounds[h].load((short)h, app);
                }
            }
        }
        sounds = newSounds;

        nHandlesTotal = nHandlesReel;

        // Plus rien a charger
        resetToLoad();
    }
}
