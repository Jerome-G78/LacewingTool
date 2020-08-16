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
// CIMAGEBANK : Stockage des images
//
//----------------------------------------------------------------------------------
package Banks;

import Services.*;
import Application.*;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Iterator;

import Runtime.MMFRuntime;

public class CImageBank implements IEnum
{
    public CRunApp app;
    CFile file;

    public class BankSlot
    {
        public short handle;

        public BankSlot (short handle)
        {
            this.handle = handle;

            useCount = 0;
            dataOffset = -1;
        }

        public int useCount;

        public int dataOffset;
        public BankImage texture;

        /* only for added images */
        public Bitmap source;

        /* used when the texture is unloaded to back up the hotspot etc. */
        public CImageInfo info_backup;
        
        public void uploadTexture()
        {
        	if (texture != null)
        		return;
        	
            if (dataOffset != -1)
            {
                file.seek (dataOffset);

                texture = new BankImage (this, file);

                if (info_backup != null)
                {
                    texture.setXAP (info_backup.xAP);
                    texture.setYAP (info_backup.yAP);

                    texture.setXSpot (info_backup.xSpot);
                    texture.setYSpot (info_backup.ySpot);

                    info_backup = null;
                }
            }
            else if (source != null)
            {
                texture = new BankImage
                        (this, CServices.getBitmapPixels (source),
                                info_backup.xSpot, info_backup.ySpot,
                                    info_backup.xAP, info_backup.yAP,
                                        source.getWidth (), source.getHeight ());
            }	
        }
    }

    public ArrayList <BankSlot> images;

    class BankImage extends CImage
    {
        public BankSlot slot;

        public BankImage (BankSlot slot, CFile file)
        {
            this.slot = slot;

            allocNative4((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0, file);
        }

        public BankImage (BankSlot slot, int [] img, int xSpot, int ySpot,
                      int xAP, int yAP, int width, int height)
        {
            this.slot = slot;

            allocNative2 ((MMFRuntime.inst.app.hdr2Options & CRunApp.AH2OPT_ANTIALIASED) != 0,
                            slot.handle, img, xSpot, ySpot, xAP, yAP, width, height);
        }

        @Override
        public void onDestroy ()
        {
            /* Save the image state */

            CImageInfo info = new CImageInfo ();

            info.xAP = getXAP ();
            info.yAP = getYAP ();
            info.xSpot = getXSpot ();
            info.ySpot = getYSpot ();

            slot.info_backup = info;
            slot.texture = null;
        }
    };

    public CImage getImageFromHandle (short handle)
    {
        if (handle < 0 || handle >= images.size ())
            return null;

        BankSlot slot = images.get (handle);
        
        if (slot == null)
        	return null;
        
        slot.uploadTexture();
        
        return slot.texture;
    }

    public CImageBank(CRunApp a)
    {
        app = a;
    }

    public void preLoad(CFile f)
    {
        file = f;

        int nMaxHandle = file.readAShort();

        images = new ArrayList <BankSlot> (nMaxHandle);

        for (short i = 0; i < nMaxHandle; ++ i)
            images.add (new BankSlot (i));

        // Repere les positions des images
        int nImg = file.readAShort();
        int n;
        for (n = 0; n < nImg; n++)
        {
            int offset = file.getFilePointer ();
            short handle = file.readAShort ();

            images.get (handle).dataOffset = offset;

            file.skipBytes (16);
            file.skipBytes (file.readAInt ());
        }
    }

    public short enumerate(short handle)
    {
        if (handle < 0 || handle > images.size ())
            return -1;

        ++ images.get (handle).useCount;

        return handle;
    }

    public void resetToLoad ()
    {
        Iterator <BankSlot> i = images.iterator ();

        while (i.hasNext ())
        {
            i.next ().useCount = 0;
        }
    }

    public void load()
    {
        Iterator <BankSlot> i = images.iterator ();

        while (i.hasNext ())
        {
            BankSlot slot = i.next ();

            if (slot.useCount != 0)
            {
            	slot.uploadTexture ();
            }
            else
            {
                if (slot.texture != null
                        && slot.dataOffset != -1) /* can't unload added images */
                {
                    slot.texture.destroy ();
                }
            }
        }
    }

    /* TODO : when can we destroy img and remove the slot? */

    public short addImage(Bitmap img, short xSpot, short ySpot, short xAP, short yAP)
    {
        BankSlot slot = new BankSlot ((short) images.size ());
        images.add (slot);

        slot.dataOffset = -1;
        slot.source = img;

        slot.texture = new BankImage
            (slot, CServices.getBitmapPixels (img), xSpot, ySpot, xAP, yAP,
                img.getWidth (), img.getHeight ());

        return slot.handle;
    }

    public void loadImageList(short[] handles)
    {
        for (int i = 0; i < handles.length; ++ i)
        {
            int handle = handles [i];

            if (handle < 0 || handle >= images.size ())
                continue;

            BankSlot slot = images.get (handle);

            if (slot.texture == null && slot.dataOffset != -1)
            {
                file.seek (slot.dataOffset);
                slot.texture = new BankImage (slot, file);
            }
        }
    }

    public CImageInfo getImageInfoEx(short nImage, float nAngle, float fScaleX, float fScaleY)
    {
        if (nImage > images.size () || nImage < 0)
            return null;

        CImage image = images.get (nImage).texture;

        if (image == null)
            return null;

        CImageInfo info = new CImageInfo ();
        image.getInfo (info, Math.round(nAngle), fScaleX, fScaleY);
        return info;
    }
}


