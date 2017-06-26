package com.example.domy.rewit;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.domy.rewit.dialogs.myDialogFragments;
import com.example.domy.rewit.interfaces.IFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;


public class MainActivity extends FragmentActivity implements IFragment,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    //SHARED PREFERENCES
    public static final String PREFS_NAME = "rewBundle";

    //USER INFO
    public static String userName;
    public static String userID;
    public static String userLocation;
    public static String userPictureLink;
    public static String userLatitude;
    public static String userLongitude;


    //FRAGMENT TAGS
    private final String FRAGMENT_HOME="FRAGMENT_HOME";
    private final String FRAGMENT_SEARCH="FRAGMENT_SEARCH";
    private final String FRAGMENT_DB_REW_LIST="FRAGMENT_DB_REW_LIST";
    private final String FRAGMENT_ENTITY="FRAGMENT_ENTITY";
    private final String FRAGMENT_REVIEW="FRAGMENT_REVIEW";
    private final String FRAGMENT_ENTITY_LIST="FRAGMENT_ENTITY_LIST";
    private final String FRAGMENT_NEARBYSEARCH="FRAGMENT_NEARBYSEARCH";
    private final String FRAGMENT_CITY_LEADERBOARD="FRAGMENT_NEARBYSEARCH";

    //LOCATION SERVICES
    public static GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    //UI
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;

    //PLAY SERVICES
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInflow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState!=null){
            //Ripristino i dati dell'utente
            userName=savedInstanceState.getString("userName");
            userID=savedInstanceState.getString("userID");
            userLocation=savedInstanceState.getString("userLocation");
            userPictureLink=savedInstanceState.getString("userPictureLink");
            Log.e("MAIN ACTIVITY","BUNDLE RESTORED!");
        }
        mDrawerList=(ListView) findViewById(R.id.left_drawer);
        mDrawerLayout=(DrawerLayout) findViewById(R.id.drawer_layout);
        //Visualizzo nella sidebar il menu' standard per l'utente non loggato
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,
                getResources().getStringArray(R.array.nav_draw_not_logged_items)));
        mDrawerToggle=new android.support.v7.app.ActionBarDrawerToggle(this,mDrawerLayout,R.string.drawer_open,R.string.drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle("OPENED");
                invalidateOptionsMenu();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                //getActionBar().setTitle("CLOSED");
                invalidateOptionsMenu();
            }
        };
        //All'avvio dell'applicazione carico il MainFragment che consente all'utente di effettuare l'accesso a FB
        if(savedInstanceState==null)
            selectItem(0,null);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //Verifico se i servizi di localizzazione sono attivi
        if(!new myDialogFragments(getApplicationContext()).isLocationEnabled()){
            myDialogFragments.AlertDialogFragment locDialog=new myDialogFragments.AlertDialogFragment();
            Bundle bundle=new Bundle();
            bundle.putString("Service",Context.LOCATION_SERVICE);
            locDialog.setArguments(bundle);
            locDialog.show(getSupportFragmentManager(),"locDialog");
        }
        buildGoogleApiClient();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position,null);
        }
    }

    private void selectItem(int position,Bundle args) {
        /*MEDIANTE TALE METODO VIENE SOSTITUITO IL FRAGMENT ATTIVO.VIENE UTILIZZATO IL BACK-STACK PER IL SALVATAGGIO DELLE
        * TRANSIZIONI IN MODO DA CONSENTIRE ALL'UTENTE UNA COMODA NAVIGAZIONE CON IL BACK BUTTON.VENGONO MEMORIZZATE SUL BACK-STACK
        * QUINDI TUTTE LE TRANSIZIONI CHE POSSONO ESSERE DISFATTE CON LA PRESSIONE DEL TASTO BACK.*/

        Fragment fragment = null;
        String fragmentTag=null;
        Boolean addToBackStack=null;
        /*Prima di avviare una qualunque transizione verifico che l'utente abbia una connessione di rete attiva,in quanto
        e' necessaria per tutte le operazioni che possono essere eseguite all'interno dell'app.In caso negativo mostro all'utente
        un dialog per avvisarlo.
         */
        if(!new myDialogFragments(getApplicationContext()).isOnline()){
            myDialogFragments.AlertDialogFragment locDialog=new myDialogFragments.AlertDialogFragment();
            Bundle bundle=new Bundle();
            bundle.putString("Service",Context.CONNECTIVITY_SERVICE);
            locDialog.setArguments(bundle);
            locDialog.show(getSupportFragmentManager(),"conDialog");
        }
        else {
            switch (position) {
                case 0:
                    //Se il fragment esiste non lo creo nuovamente
                    fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_HOME);
                    if (fragment == null) {
                        fragment = new MainFragment();
                        fragmentTag = FRAGMENT_HOME;
                    }
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    addToBackStack = false;
                /*La transizione verso il fragment iniziale non va mai messa sullo stack in quanto non si è mai
                * intenzionati a disfarla.Si verifica inizialmente quando viene caricato per la prima volta il FRAGMENT_HOME e ogni qualvolta
                * l'utente seleziona Home nel navigation-drawer.In entrambi i casi il back-button non avrebbe senso,quindi setto la variabile
                * booleana addToBackStack=false e svuoto il back-stack.
                */
                    break;
                case 1:
                    fragment = new SearchEntity();
                    fragmentTag = FRAGMENT_SEARCH;
                    args = new Bundle();
                    args.putChar("Choose", 's');
                    fragment.setArguments(args);
                    addToBackStack = getSupportFragmentManager().findFragmentById(R.id.content_frame).getTag().equals(FRAGMENT_HOME);
                    if (!addToBackStack) {
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        selectItem(0, null);
                        addToBackStack = true;
                    }
                /*Nel caso in cui l'utente selezioni una delle categorie nel navigation drawer la transizione viene aggiunta sul back-stack solo
                * se proviene dal fragment iniziale (FRAGMENT_HOME).In caso contrario viene svuotato il back-stack,viene sostituito il fragment
                * attuale con il FRAGMENT_HOME (senza memorizzazione sul back-stack) e infine viene sostituito quest'ultimo a quello corrispondente alla scelta effettuata dall'utente
                * nel navigation drawer (con memorizzazione sul back-stack).*/
                    break;
                case 2:
                    //Visualizzo la lista degli obiettivi
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), 0);
                    }
                    break;
                case 3:
                    //Visualizzo la classifica
                    if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                                getResources().getString(R.string.leaderboard_id)), 0);
                    }
                    break;
                case 4:
                    fragment = new CityLeaderboard();
                    fragmentTag = FRAGMENT_CITY_LEADERBOARD;
                    fragment.setArguments(args);
                    addToBackStack = getSupportFragmentManager().findFragmentById(R.id.content_frame).getTag().equals(FRAGMENT_HOME);
                    if (!addToBackStack) {
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        selectItem(0, null);
                        addToBackStack = true;
                    }
                    break;
                case 5:
                    fragment = new SearchEntity();
                    fragmentTag = FRAGMENT_REVIEW;
                    args = new Bundle();
                    args.putChar("Choose", 'r');
                    fragment.setArguments(args);
                    addToBackStack = getSupportFragmentManager().findFragmentById(R.id.content_frame).getTag().equals(FRAGMENT_HOME);
                    if (!addToBackStack) {
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        selectItem(0, null);
                        addToBackStack = true;
                    }
                    break;
                case 6:
                    fragment = new Entity();
                    fragmentTag = FRAGMENT_ENTITY;
                    fragment.setArguments(args);
                    addToBackStack = true;
                    break;
                case 7:
                    fragment = new EntityList();
                    fragmentTag = FRAGMENT_ENTITY_LIST;
                    fragment.setArguments(args);
                    addToBackStack = true;
                    break;
                case 8:
                    fragment = new NearbySearch();
                    fragmentTag = FRAGMENT_NEARBYSEARCH;
                    fragment.setArguments(args);
                    addToBackStack = true;
                    break;
                case 9:
                    fragment = new DbReviewList();
                    fragmentTag = FRAGMENT_DB_REW_LIST;
                    fragment.setArguments(args);
                    addToBackStack = true;
                    break;
                default:
                    break;
            }
            if (fragment != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                if (addToBackStack)
                    fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.content_frame, fragment, fragmentTag).commit();
                // update selected item and title, then close the drawer
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Salvo i dati dell'utente
        outState.putString("userName",userName);
        outState.putString("userID",userID);
        outState.putString("userLocation",userLocation);
        outState.putString("userPictureLink",userPictureLink);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        if(menu.findItem(R.id.action_search)!=null)
            menu.findItem(R.id.action_search).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStatusChange(boolean isConnected) {
        /*
        Metodo che consente al MainFragment di aggiornare la lista degli elementi contenuti nel Navigation Drawer
        una volta che l'utente ha effettuato il login a FB
         */
        if(isConnected)
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item,
                    getResources().getStringArray(R.array.nav_draw_logged_items)));
        else
            mDrawerList.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_list_item,
                    getResources().getStringArray(R.array.nav_draw_not_logged_items)));
    }

    @Override
    public void swapActiveFragment(int position,Bundle args) {
        /*
        Mediante tale metodo,contenuto nell'interfaccia IFragment,un fragment e' in grado di richiedere all'activity principale
        l'avvio di un nuovo fragment,specificando anche i parametri che bisogna passarvi.
         */
        selectItem(position,args);
    }

    //LOCATION SERVICES & ACHIEVEMENTS API
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.e("Latitude", String.valueOf(mLastLocation.getLatitude()));
            Log.e("Longitude", String.valueOf(mLastLocation.getLongitude()));
            userLatitude = String.valueOf(mLastLocation.getLatitude());
            userLongitude = String.valueOf(mLastLocation.getLongitude());
        }
    }



    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Error", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mAutoStartSignInflow) {
            mAutoStartSignInflow = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN,"other error")) {
                mResolvingConnectionFailure = false;
            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RC_SIGN_IN) {
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, 0);
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Error", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
