package thealphalabs.alphaglasslauncher.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Created by yeol on 15. 8. 21.
 */
public class AppInfoProvider {
    private PackageManager mPackageManager;
    private static AppInfoProvider instance;
    private List<ResolveInfo> AppInfos;
    private Context context;

    private AppInfoProvider(Context context){
        this.context=context;
        mPackageManager=context.getPackageManager();
        getAppList(mPackageManager);
    }

    private void getAppList(PackageManager pm){
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        AppInfos=pm.queryIntentActivities(mainIntent,0);
    }

    public static AppInfoProvider getInstance(Context context){
        if(instance == null){
            instance=new AppInfoProvider(context);
        }
        return instance;
    }

    public void call(ActivityInfo activityInfo){
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClassName(activityInfo.applicationInfo.packageName, activityInfo.name);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public String getName(int position) {
        ResolveInfo info=AppInfos.get(position);
        return info.loadLabel(mPackageManager).toString();
    }

    public Drawable getIcon(int position) {
        ResolveInfo info=AppInfos.get(position);
        return info.loadIcon(mPackageManager);
    }

}
