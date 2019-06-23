package com.vluver.beta

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.support.v4.view.GravityCompat
import android.view.MenuItem
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout

import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mancj.materialsearchbar.MaterialSearchBar
import com.vluver.beta.activities.PostActivity
import com.vluver.beta.activities.ServiceBusiness.RegisterBusiness
import com.vluver.beta.activities.searchinvluver.SearchInVluver
import com.vluver.beta.adapter.BottomMenuItemAdapter
import com.vluver.beta.adapter.BottomNavigationViewPager
import com.vluver.beta.utils.FragmentHistory
import com.vluver.beta.utils.GlideLoadImages

import java.io.File
import java.util.Objects

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener {
    private  var frag: Fragment? = null
    private var fragmentHistory: FragmentHistory? = null
    private var mAuth: FirebaseAuth? = null
    private var bottomNavigation: BottomNavigationView? = null
    internal lateinit var floatingActionButton: FloatingActionButton
    private var names: TextView? = null
    private var email: TextView? = null
    private var avatar: ImageView? = null
    internal lateinit var searchBar: MaterialSearchBar
    private var drawer: DrawerLayout? = null
    private var registerbusiness: LinearLayout? = null
    private var blurry: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        floatingActionButton = findViewById<View>(R.id.fab_post) as FloatingActionButton
        floatingActionButton.setOnClickListener { startActivity(Intent(this@MainActivity, PostActivity::class.java)) }

        drawer = findViewById(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        names = headerView.findViewById<View>(R.id.name_user) as TextView
        email = headerView.findViewById<View>(R.id.email_user) as TextView
        avatar = headerView.findViewById<View>(R.id.avatar_user) as ImageView
        blurry = headerView.findViewById<View>(R.id.blurryimg) as ImageView
        registerbusiness = headerView.findViewById<View>(R.id.register_business) as LinearLayout
        registerbusiness!!.setOnClickListener { startActivity(Intent(this@MainActivity, RegisterBusiness::class.java)) }
        searchBar = findViewById(R.id.searchBar)

        navigationView.setNavigationItemSelectedListener(this)
        searchBar.setOnSearchActionListener(this)
        searchBar.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchInVluver::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
        searchBar.inflateMenu(R.menu.main)
        fragmentHistory = FragmentHistory()
        mBottomNavigationViewPager = findViewById<View>(R.id.contentContainer) as BottomNavigationViewPager
        mBottomMenuItemAdapter = BottomMenuItemAdapter(supportFragmentManager)
        mBottomNavigationViewPager.adapter = mBottomMenuItemAdapter
        mBottomNavigationViewPager.setPagingEnable(true)
        frag = mBottomMenuItemAdapter.currentFragment
        bottomNavigation = findViewById<View>(R.id.bottom_navigation) as BottomNavigationView
        fragmentHistory!!.push(0)
        mBottomNavigationViewPager.offscreenPageLimit = 4
        bottomNavigation!!.setOnNavigationItemSelectedListener { menuItem ->
            var numero = 0
            when (menuItem.itemId) {
                R.id.feed -> {
                    floatingActionButton.show()
                    mBottomNavigationViewPager.setCurrentItem(0, true)
                    frag = mBottomMenuItemAdapter.currentFragment
                }
                R.id.ads -> {
                    numero = 1
                    floatingActionButton.hide()
                    mBottomNavigationViewPager.setCurrentItem(1, true)
                    frag = mBottomMenuItemAdapter.currentFragment
                }
                R.id.map -> {
                    numero = 2
                    floatingActionButton.hide()
                    mBottomNavigationViewPager.setCurrentItem(2, true)
                    frag = mBottomMenuItemAdapter.currentFragment
                }
                R.id.notifications -> {
                    numero = 3
                    floatingActionButton.hide()
                    mBottomNavigationViewPager.setCurrentItem(3, true)
                    frag = mBottomMenuItemAdapter.currentFragment
                }
                else -> {
                    numero = 0
                    floatingActionButton.show()
                    mBottomNavigationViewPager.setCurrentItem(0, true)
                    frag = mBottomMenuItemAdapter.currentFragment
                }
            }
            fragmentHistory!!.push(numero)
            true
        }
        mBottomNavigationViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                when (i) {
                    0 -> {
                        bottomNavigation!!.menu.findItem(R.id.feed).isChecked = true
                        floatingActionButton.show()
                    }
                    1 -> {
                        bottomNavigation!!.menu.findItem(R.id.ads).isChecked = true
                        floatingActionButton.hide()
                    }
                    2 -> {
                        bottomNavigation!!.menu.findItem(R.id.map).isChecked = true
                        floatingActionButton.hide()
                    }
                    3 -> {
                        bottomNavigation!!.menu.findItem(R.id.notifications).isChecked = true
                        floatingActionButton.hide()
                    }
                }
                fragmentHistory!!.push(i)
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })
        //bottomNavigation.setSelectedItemId(R.id.feed);
        //setCurrentItem(0);
    }


    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (fragmentHistory!!.stackSize > 1) {
                val position = fragmentHistory!!.popPrevious()
                fragmentHistory!!.pop()
                when (position) {
                    0 -> bottomNavigation!!.selectedItemId = R.id.feed
                    1 -> bottomNavigation!!.selectedItemId = R.id.ads
                    2 -> bottomNavigation!!.selectedItemId = R.id.map
                    3 -> bottomNavigation!!.selectedItemId = R.id.notifications
                    else -> bottomNavigation!!.selectedItemId = R.id.feed
                }
            } else {
                super.onBackPressed()

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId



        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
            logout()
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getdataUser(avatarurl: String) {
        names!!.text = Objects.requireNonNull<FirebaseUser>(mAuth!!.currentUser).displayName
        email!!.text = mAuth!!.currentUser!!.email
        GlideLoadImages.loadAvatar(this@MainActivity, avatarurl, avatar)
        GlideLoadImages.setBlurrimg(this@MainActivity, avatarurl, blurry)

    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth!!.currentUser
        if (currentUser == null) {
            updateUI()
        } else {
            val avatar = "https://graph.facebook.com/" + currentUser.providerData[1].uid + "/picture?height=150"
            getdataUser(avatar)
        }
    }

    private fun updateUI() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)

    }

    override fun onResume() {
        val state = mBottomNavigationViewPager.currentItem
        //Toast.makeText(this, ""+state, Toast.LENGTH_SHORT).show();
        super.onResume()
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        deleteCache(this)
        salir()
        // Intent get = getIntent();
        //String typelogin = get.getStringExtra("login_type");
    }

    fun salir() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)

    }

    override fun onSearchStateChanged(enabled: Boolean) {}

    override fun onSearchConfirmed(text: CharSequence) {

    }

    @SuppressLint("RtlHardcoded")
    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_NAVIGATION -> drawer!!.openDrawer(Gravity.LEFT)
            MaterialSearchBar.BUTTON_SPEECH -> {
            }
            MaterialSearchBar.BUTTON_BACK -> searchBar.disableSearch()
        }
    }

    companion object {
        lateinit var mBottomNavigationViewPager: BottomNavigationViewPager
        lateinit var mBottomMenuItemAdapter: BottomMenuItemAdapter
        fun deleteCache(context: Context) {
            try {
                val dir = context.cacheDir
                deleteDir(dir)
            } catch (e: Exception) {
            }

        }

        fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
                return dir.delete()
            } else return if (dir != null && dir.isFile) {
                dir.delete()
            } else {
                false
            }
        }
    }

}
