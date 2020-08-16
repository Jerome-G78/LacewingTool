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
package Runtime;

import Services.CServices;
import android.accounts.Account;
import android.accounts.AccountManager;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CrashReporter implements Thread.UncaughtExceptionHandler
{
    private static String info = "";

    public static void addInfo (String subject, String data)
    {
        if (info != "")
            info += "\r\n";

        info += "    " + subject + " : " + data;
    }

    public void uncaughtException (Thread t, final Throwable e)
    {
        if (!MMFRuntime.inst.enableCrashReporting)
        {
            Log.Log ("Crash reporting disabled");
            Thread.getDefaultUncaughtExceptionHandler().uncaughtException (t, e);

            return;
        }

        CrashReporter.addInfo ("Device ID", CServices.getAndroidID());

        Log.Log ("Reporting exception ...");

        ArrayList <NameValuePair> postData = new ArrayList <NameValuePair> ();

        postData.add(new NameValuePair()
        {
            public String getName()
            {
                return "report";
            }

            public String getValue()
            {
                String report = "";
                
                try
                {
                    Account [] accounts = AccountManager.get (MMFRuntime.inst).getAccounts();
                    
                    if (accounts != null)
                    {
                        for (Account account : accounts)
                        {
                            if (account.name != null)
                                report += account.name + "/";
                        }
                    }
                }
                catch (Throwable t)
                {
                }

                report += MMFRuntime.version + " : " + e.toString ();

                StackTraceElement [] stack = e.getStackTrace ();

                Log.Log ("Stack has " + stack.length + " elements");

                for (int i = 0; i < stack.length; ++ i)
                    report += "\r\n    " + stack [i].toString ();

                if (info != "")
                {
                    report += "\r\n\r\n";
                    report += info;
                }

                return report;
            }
        });

        HttpPost post = new HttpPost ("http://bugs.clickteam.com/report.php");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");

        try
        {   post.setEntity (new UrlEncodedFormEntity (postData));
        }
        catch(Throwable _t)
        {   return;
        }

        DefaultHttpClient client = new DefaultHttpClient();

        HttpParams params = post.getParams();
        HttpProtocolParams.setUseExpectContinue (params, false);
        post.setParams(params);

        HttpResponse response;

        try
        {   response = client.execute (post);

            BufferedReader reader = new BufferedReader
                    (new InputStreamReader (response.getEntity().getContent()));

            String line;

            while((line = reader.readLine()) != null) {}
        }
        catch (Throwable _t)
        {   return;
        }

        Log.Log ("Reported exception: " + e.toString ());

        Thread.getDefaultUncaughtExceptionHandler().uncaughtException (t, e);
    }
}
