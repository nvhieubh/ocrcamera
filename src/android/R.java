package cordova.plugin.ocrcamera;

import android.app.Activity;
import android.content.Context;
import java.lang.reflect.Field;

/**
 * R replacement for PhoneGap Build.
 *
 * ([^.\w])R\.(\w+)\.(\w+)
 * $1fakeR("$2", "$3")
 *
 * @author Maciej Nux Jaros
 */
public class R {
    private Context context;
    private String packageName;

    public R(Activity activity) {
        context = activity.getApplicationContext();
        packageName = context.getPackageName();
    }

    public R(Context context) {
        this.context = context;
        packageName = context.getPackageName();
    }

    public int getId(String group, String key) {
        return context.getResources().getIdentifier(key, group, packageName);
    }

    public static int getId(Context context, String group, String key) {
        return context.getResources().getIdentifier(key, group, context.getPackageName());
    }

    public static final int[] getStyleableIntArray(Context context, String name )
	{
	    try
	    {
	        Field[] fields2 = Class.forName(context.getPackageName() + ".R$styleable" ).getFields();
	        for ( Field f : fields2 )
	        {
	            if ( f.getName().equals( name ) )
	            {
	                int[] ret = (int[])f.get( null );
	                return ret;
	            }
	        }
	    }
	    catch ( Throwable t ){}
	    return null;
	}
}
