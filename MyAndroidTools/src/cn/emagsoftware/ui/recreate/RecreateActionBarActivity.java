package cn.emagsoftware.ui.recreate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class RecreateActionBarActivity extends ActionBarActivity
{

    private static final String EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE = "android.intent.extra.RECREATEACTIONBARACTIVITY_OUTSTATE";

    private Bundle              outState                                = null;
    private boolean             isAtFront                               = false;
    private boolean             shouldRecreateNextTime                  = false;

    @Override
    protected final void onCreate(Bundle savedInstanceState)
    {
        RecreateManager.addRecreateActivity(this);
        Intent intent = getIntent();
        outState = intent.getBundleExtra(EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE);
        if (outState == null)
        {
            onCreateImpl(savedInstanceState);
        } else
        {
            intent.removeExtra(EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE);
            onCreateImpl(outState);
        }
    }

    protected void onCreateImpl(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected final void onStart()
    {
        onStartImpl();
        if (outState != null)
        {
            onRestoreInstanceState(outState);
            outState = null;
        }
    }

    protected void onStartImpl()
    {
        super.onStart();
    }

    @Override
    protected final void onResume()
    {
        isAtFront = true;
        if (shouldRecreateNextTime)
        {
            shouldRecreateNextTime = false;
            recreateMeImmediately();
            return;
        }
        onResumeImpl();
    }

    protected void onResumeImpl()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isAtFront = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        RecreateManager.removeRecreateActivity(this);
    }

    public boolean isAtFront()
    {
        return isAtFront;
    }

    public void recreateMe()
    {
        if (isAtFront())
            recreateMeImmediately();
        else
            shouldRecreateNextTime = true;
    }

    private void recreateMeImmediately()
    {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Bundle outState = new Bundle();
        onSaveInstanceState(outState);
        finish();
        overridePendingTransition(0, 0);
        intent.putExtra(EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE, outState);
        startActivity(intent);
    }

}
