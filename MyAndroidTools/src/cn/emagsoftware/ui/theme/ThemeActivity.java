package cn.emagsoftware.ui.theme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public abstract class ThemeActivity extends Activity
{

    private static final String EXTRA_THEMEACTIVITY_OUTSTATE = "android.intent.extra.THEMEACTIVITY_OUTSTATE";

    private Bundle              outState                     = null;
    private boolean             isAtFront                    = false;
    private boolean             shouldRecreateNextTime       = false;

    @Override
    protected final void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        ThemeEngine.addThemeActivity(this);
        getLayoutInflater().setFactory(ThemeFactory.createOrUpdateInstance(this, ThemeEngine.CUR_PACKAGENAME, ThemeEngine.CUR_GENERALTHEME_NAME));
        Intent intent = getIntent();
        outState = intent.getBundleExtra(EXTRA_THEMEACTIVITY_OUTSTATE);
        if (outState == null)
        {
            onCreateImpl(savedInstanceState);
        } else
        {
            intent.removeExtra(EXTRA_THEMEACTIVITY_OUTSTATE);
            onCreateImpl(outState);
        }
    }

    protected void onCreateImpl(Bundle savedInstanceState)
    {
    }

    @Override
    protected final void onStart()
    {
        // TODO Auto-generated method stub
        super.onStart();
        onStartImpl();
        if (outState != null)
        {
            onRestoreInstanceState(outState);
            outState = null;
        }
    }

    protected void onStartImpl()
    {
    }

    @Override
    protected final void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        isAtFront = true;
        if (shouldRecreateNextTime)
        {
            shouldRecreateNextTime = false;
            recreateImmediately();
            return;
        }
        onResumeImpl();
    }

    protected void onResumeImpl()
    {
    }

    @Override
    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        isAtFront = false;
    }

    @Override
    protected void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
        ThemeEngine.removeThemeActivity(this);
    }

    public boolean isAtFront()
    {
        return isAtFront;
    }

    public void recreate()
    {
        if (isAtFront())
            recreateImmediately();
        else
            shouldRecreateNextTime = true;
    }

    private void recreateImmediately()
    {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        finish();
        overridePendingTransition(0, 0);
        intent.putExtra(EXTRA_THEMEACTIVITY_OUTSTATE, outState);
        startActivity(intent);
    }

}
