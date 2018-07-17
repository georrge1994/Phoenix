package com.phoenix.kspt.people


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ImageView
import com.google.android.gms.common.util.NumberUtils
import com.phoenix.kspt.Application.Companion.firebase
import com.phoenix.kspt.activites.MainWindowInterface
import com.phoenix.kspt.R
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.profile.ProfileFragment
import com.phoenix.kspt.models.User
import com.phoenix.kspt.utils.*
import kotlinx.android.synthetic.main.fragment_people.*
import java.util.ArrayList

/**
 * Fragment contain list of users in system and simple searching by list.
 */
class PeopleFragment : FirebaseFragment(), SearchView.OnQueryTextListener {
    private lateinit var adapter: PeopleAdapter
    private lateinit var searchView: SearchView
    private var listener: MainWindowInterface? = null
    private var allUsers: ArrayList<User> = ArrayList()
    private var query: String = ""                          // searching codeword

    companion object {
        fun newInstance(people: List<User>, groupId: String): PeopleFragment {
            val fragment = PeopleFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList(PEOPLE_LIST, ArrayList(people))
            bundle.putString(GROUP_ID, groupId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener?.setTitle(getString(R.string.stuff))

        // after professor click to group in list
        if (arguments != null) {
            allUsers = arguments!!.getParcelableArrayList(PEOPLE_LIST)
            query = arguments!!.getString(GROUP_ID)
            setupRecyclerView(allUsers)
            onQueryTextChange(query)

        // when user open an user list from menu
        } else {
            listener?.visibilityWait(true)
            firebase.fetchAllUsers().subscribe {
                if (it != null) {
                    allUsers.clear()
                    allUsers.addAll(it.sortedWith(CompareUsers))
                    listener?.visibilityWait(false)
                    setupRecyclerView(allUsers)
                }
            }
        }
    }

    class CompareUsers {
        companion object : Comparator<User> {
            override fun compare(user1: User, user2: User): Int = user1.firstName.compareTo(user2.firstName)
        }
    }

    /**
     * Init recycler view after fetching data from firebase
     */
    private fun setupRecyclerView(users: ArrayList<User>) {
        adapter = PeopleAdapter(context!!, users, object : ClickToProfile {
            override fun openProfile(user: User) {
                adapter.notifyDataSetChanged()
                listener?.replaceFragment(ProfileFragment.newInstance(user), true)
            }
        })
        recyclerView.adapter = adapter
    }

    /**
     * Init toolbar and searching view
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_people, menu)

        // Search setting
        val searchMenuItem = menu.findItem(R.id.search)
        searchView = searchMenuItem.actionView as SearchView
        // set colors and hint text
        val searchAutoComplete = searchView.findViewById<SearchView.SearchAutoComplete>(android.support.v7.appcompat.R.id.search_src_text)
        searchAutoComplete.setHintTextColor(ContextCompat.getColor(context!!, R.color.disableColor))
        searchAutoComplete.setTextColor(ContextCompat.getColor(context!!, R.color.textColor))
        searchAutoComplete.hint = getString(R.string.searching)
        if (arguments != null) {
            val groupId = arguments!!.getString(GROUP_ID)
            searchAutoComplete.setText(groupId)
        }
        // Color for searchField background
        val searchPlate = searchView.findViewById<View>(android.support.v7.appcompat.R.id.search_plate)
        searchPlate.setBackgroundResource(R.drawable.rounded_white_rectangle)

        // set close image
        val searchCloseIcon = searchView.findViewById<ImageView>(android.support.v7.appcompat.R.id.search_close_btn)
        searchCloseIcon.setImageResource(R.drawable.ic_close_black_24dp)

        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        val filteredUsers = ArrayList<User>()

        if (query!!.isEmpty()) {
            filteredUsers.addAll(allUsers)

        } else {
            this.query = query.toLowerCase()

            for (user in allUsers) {
                // create a simple sentence from user data
                val text = (user.firstName + " " + user.lastName + " " +
                        user.userStatus + " " + user.groupId + " " + "${user.percentOvt1}%").toLowerCase()

                // checking keyword in sentence
                if (text.contains(query))
                    filteredUsers.add(user)
                else if (NumberUtils.isNumeric(query) && query.toDouble() <= user.percentOvt1)
                    filteredUsers.add(user)
            }
        }

        setupRecyclerView(filteredUsers)

        return true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (listener == null && context is MainWindowInterface)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
