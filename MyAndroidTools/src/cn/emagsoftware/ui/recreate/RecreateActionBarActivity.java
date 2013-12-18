package cn.emagsoftware.ui.recreate;

import android.content.Intent;
import android.os.Bundle;

import cn.emagsoftware.ui.GenericActionBarActivity;

public abstract class RecreateActionBarActivity extends GenericActionBarActivity
{

    private static final String EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE = "android.intent.extra.RECREATEACTIONBARACTIVITY_OUTSTATE";

    private Bundle              outState                                = null;
    private boolean             isAtFront                               = false;
    private boolean             shouldRecreateNextTime                  = false;

    @Override
    protected final void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    protected final void onStart()
    {
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
        intent.putExtra(EXTRA_RECREATEACTIONBARACTIVITY_OUTSTATE, outState);
        startActivity(intent);
    }

}
