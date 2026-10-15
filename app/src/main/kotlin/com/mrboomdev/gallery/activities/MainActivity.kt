package com.mrboomdev.gallery.activities

import android.content.ClipData
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.fossify.commons.dialogs.CreateNewFolderDialog
import org.fossify.commons.dialogs.FilePickerDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.appLaunched
import org.fossify.commons.extensions.appLockManager
import org.fossify.commons.extensions.areSystemAnimationsEnabled
import org.fossify.commons.extensions.beGone
import org.fossify.commons.extensions.beVisible
import org.fossify.commons.extensions.beVisibleIf
import org.fossify.commons.extensions.checkWhatsNew
import org.fossify.commons.extensions.deleteFiles
import org.fossify.commons.extensions.getDoesFilePathExist
import org.fossify.commons.extensions.getFileCount
import org.fossify.commons.extensions.getFilePublicUri
import org.fossify.commons.extensions.getFilenameFromPath
import org.fossify.commons.extensions.getIsPathDirectory
import org.fossify.commons.extensions.getLatestMediaByDateId
import org.fossify.commons.extensions.getLatestMediaId
import org.fossify.commons.extensions.getMimeType
import org.fossify.commons.extensions.getProperPrimaryColor
import org.fossify.commons.extensions.getProperSize
import org.fossify.commons.extensions.getProperTextColor
import org.fossify.commons.extensions.getStorageDirectories
import org.fossify.commons.extensions.getTimeFormat
import org.fossify.commons.extensions.handleHiddenFolderPasswordProtection
import org.fossify.commons.extensions.handleLockedFolderOpening
import org.fossify.commons.extensions.hasAllPermissions
import org.fossify.commons.extensions.hasOTGConnected
import org.fossify.commons.extensions.hasPermission
import org.fossify.commons.extensions.hideKeyboard
import org.fossify.commons.extensions.internalStoragePath
import org.fossify.commons.extensions.isExternalStorageManager
import org.fossify.commons.extensions.isGif
import org.fossify.commons.extensions.isGone
import org.fossify.commons.extensions.isImageFast
import org.fossify.commons.extensions.isMediaFile
import org.fossify.commons.extensions.isPathOnOTG
import org.fossify.commons.extensions.isRawFast
import org.fossify.commons.extensions.isSvg
import org.fossify.commons.extensions.isVideoFast
import org.fossify.commons.extensions.isVisible
import org.fossify.commons.extensions.launchMoreAppsFromUsIntent
import org.fossify.commons.extensions.recycleBinPath
import org.fossify.commons.extensions.sdCardPath
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.extensions.toFileDirItem
import org.fossify.commons.extensions.toast
import org.fossify.commons.extensions.underlineText
import org.fossify.commons.extensions.viewBinding
import org.fossify.commons.helpers.DAY_SECONDS
import org.fossify.commons.helpers.FAVORITES
import org.fossify.commons.helpers.IS_FROM_GALLERY
import org.fossify.commons.helpers.PERMISSION_READ_STORAGE
import org.fossify.commons.helpers.SORT_BY_DATE_MODIFIED
import org.fossify.commons.helpers.SORT_BY_DATE_TAKEN
import org.fossify.commons.helpers.SORT_BY_RANDOM
import org.fossify.commons.helpers.SORT_BY_SIZE
import org.fossify.commons.helpers.SORT_USE_NUMERIC_VALUE
import org.fossify.commons.helpers.VIEW_TYPE_GRID
import org.fossify.commons.helpers.VIEW_TYPE_LIST
import org.fossify.commons.helpers.ensureBackgroundThread
import org.fossify.commons.helpers.isRPlus
import org.fossify.commons.models.FileDirItem
import org.fossify.commons.models.RadioItem
import org.fossify.commons.models.Release
import org.fossify.commons.views.MyGridLayoutManager
import org.fossify.commons.views.MyRecyclerView
import com.mrboomdev.gallery.BuildConfig
import com.mrboomdev.gallery.R
import com.mrboomdev.gallery.adapters.DirectoryAdapter
import com.mrboomdev.gallery.adapters.MediaAdapter
import com.mrboomdev.gallery.asynctasks.GetMediaAsynctask
import com.mrboomdev.gallery.databases.GalleryDatabase
import com.mrboomdev.gallery.databinding.ActivityMainBinding
import com.mrboomdev.gallery.dialogs.ChangeGroupingDialog
import com.mrboomdev.gallery.dialogs.ChangeSortingDialog
import com.mrboomdev.gallery.dialogs.ChangeViewTypeDialog
import com.mrboomdev.gallery.dialogs.FilterMediaDialog
import com.mrboomdev.gallery.dialogs.GrantAllFilesDialog
import com.mrboomdev.gallery.extensions.addTempFolderIfNeeded
import com.mrboomdev.gallery.extensions.config
import com.mrboomdev.gallery.extensions.createDirectoryFromMedia
import com.mrboomdev.gallery.extensions.deleteDBPath
import com.mrboomdev.gallery.extensions.directoryDB
import com.mrboomdev.gallery.extensions.getCachedDirectories
import com.mrboomdev.gallery.extensions.getCachedMedia
import com.mrboomdev.gallery.extensions.getDirectorySortingValue
import com.mrboomdev.gallery.extensions.getDirsToShow
import com.mrboomdev.gallery.extensions.getDistinctPath
import com.mrboomdev.gallery.extensions.getFavoritePaths
import com.mrboomdev.gallery.extensions.getNoMediaFoldersSync
import com.mrboomdev.gallery.extensions.getOTGFolderChildrenNames
import com.mrboomdev.gallery.extensions.getSortedDirectories
import com.mrboomdev.gallery.extensions.handleExcludedFolderPasswordProtection
import com.mrboomdev.gallery.extensions.handleMediaManagementPrompt
import com.mrboomdev.gallery.extensions.isDownloadsFolder
import com.mrboomdev.gallery.extensions.launchAbout
import com.mrboomdev.gallery.extensions.launchCamera
import com.mrboomdev.gallery.extensions.launchGesturePlayer
import com.mrboomdev.gallery.extensions.launchSettings
import com.mrboomdev.gallery.extensions.mediaDB
import com.mrboomdev.gallery.extensions.movePathsInRecycleBin
import com.mrboomdev.gallery.extensions.movePinnedDirectoriesToFront
import com.mrboomdev.gallery.extensions.openPath
import com.mrboomdev.gallery.extensions.openRecycleBin
import com.mrboomdev.gallery.extensions.removeInvalidDBDirectories
import com.mrboomdev.gallery.extensions.setCollapsibleOnScroll
import com.mrboomdev.gallery.extensions.storeDirectoryItems
import com.mrboomdev.gallery.extensions.tryDeleteFileDirItem
import com.mrboomdev.gallery.extensions.updateDBDirectory
import com.mrboomdev.gallery.extensions.updateWidgets
import com.mrboomdev.gallery.helpers.DIRECTORY
import com.mrboomdev.gallery.helpers.GET_ANY_INTENT
import com.mrboomdev.gallery.helpers.GET_IMAGE_INTENT
import com.mrboomdev.gallery.helpers.GET_VIDEO_INTENT
import com.mrboomdev.gallery.helpers.GROUP_BY_DATE_TAKEN_DAILY
import com.mrboomdev.gallery.helpers.GROUP_BY_DATE_TAKEN_MONTHLY
import com.mrboomdev.gallery.helpers.GROUP_BY_LAST_MODIFIED_DAILY
import com.mrboomdev.gallery.helpers.GROUP_BY_LAST_MODIFIED_MONTHLY
import com.mrboomdev.gallery.helpers.GROUP_DESCENDING
import com.mrboomdev.gallery.helpers.GridSpacingItemDecoration
import com.mrboomdev.gallery.helpers.LOCATION_INTERNAL
import com.mrboomdev.gallery.helpers.MAX_COLUMN_COUNT
import com.mrboomdev.gallery.helpers.MONTH_MILLISECONDS
import com.mrboomdev.gallery.helpers.MediaFetcher
import com.mrboomdev.gallery.helpers.PATH
import com.mrboomdev.gallery.helpers.PICKED_PATHS
import com.mrboomdev.gallery.helpers.RECYCLE_BIN
import com.mrboomdev.gallery.helpers.SET_WALLPAPER_INTENT
import com.mrboomdev.gallery.helpers.SHOW_ALL
import com.mrboomdev.gallery.helpers.SHOW_TEMP_HIDDEN_DURATION
import com.mrboomdev.gallery.helpers.SLIDESHOW_START_ON_ENTER
import com.mrboomdev.gallery.helpers.SKIP_AUTHENTICATION
import com.mrboomdev.gallery.helpers.TYPE_GIFS
import com.mrboomdev.gallery.helpers.TYPE_IMAGES
import com.mrboomdev.gallery.helpers.TYPE_RAWS
import com.mrboomdev.gallery.helpers.TYPE_SVGS
import com.mrboomdev.gallery.helpers.TYPE_VIDEOS
import com.mrboomdev.gallery.helpers.VIDEO_PLAYER_APP
import com.mrboomdev.gallery.helpers.VIDEO_PLAYER_SYSTEM
import com.mrboomdev.gallery.helpers.getDefaultFileFilter
import com.mrboomdev.gallery.helpers.getPermissionToRequest
import com.mrboomdev.gallery.helpers.getPermissionsToRequest
import com.mrboomdev.gallery.interfaces.DirectoryOperationsListener
import com.mrboomdev.gallery.interfaces.MediaOperationsListener
import com.mrboomdev.gallery.jobs.NewPhotoFetcher
import com.mrboomdev.gallery.models.Directory
import com.mrboomdev.gallery.models.Medium
import com.mrboomdev.gallery.models.ThumbnailItem
import com.mrboomdev.gallery.models.ThumbnailSection
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : SimpleActivity(), DirectoryOperationsListener, MediaOperationsListener {
    override var isSearchBarEnabled = true
    
    companion object {
        private const val PICK_MEDIA = 2
        private const val PICK_WALLPAPER = 3
        private const val LAST_MEDIA_CHECK_PERIOD = 3000L
        private const val IME_VISIBILITY_THRESHOLD = 0.15f
    }

    private var mIsPickImageIntent = false
    private var mIsPickVideoIntent = false
    private var mIsGetImageContentIntent = false
    private var mIsGetVideoContentIntent = false
    private var mIsGetAnyContentIntent = false
    private var mIsSetWallpaperIntent = false
    private var mAllowPickingMultiple = false
    private var mIsThirdPartyIntent = false
    private var mIsGettingDirs = false
    private var mLoadedInitialPhotos = false
    private var mShouldStopFetching = false
    private var mWasDefaultFolderChecked = false
    private var mWasMediaManagementPromptShown = false
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L

    // used at "Group direct subfolders" for navigation
    private var mCurrentPathPrefix = ""

    // used at "Group direct subfolders" for navigating Up with the back button
    private var mOpenedSubfolders = arrayListOf("")

    private var mDateFormat = ""
    private var mTimeFormat = ""
    private var mLastMediaHandler = Handler()
    private var mTempShowHiddenHandler = Handler()
    private var mZoomListener: MyRecyclerView.MyZoomListener? = null
    private var mLastMediaFetcher: MediaFetcher? = null
    private var mDirs = ArrayList<Directory>()
    private var mDirsIgnoringSearch = ArrayList<Directory>()

    private var mStoredAnimateGifs = true
    private var mStoredCropThumbnails = true
    private var mStoredScrollHorizontally = true
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredStyleString = ""

    // Photos tab
    private var mShowingPhotos = false
    private var mPhotos = ArrayList<ThumbnailItem>()
    private var mIsGettingPhotos = false
    private var mPhotosLoadedInitial = false
    private var mPhotosLastSearchedText = ""
    private var mPhotosWereVisibleInFullscreen = false
    private var mShowPhotosLoadingIndicator = true
    private var mPhotosLatestMediaId = 0L
    private var mPhotosLatestMediaDateId = 0L
    private var mCurrPhotosAsyncTask: GetMediaAsynctask? = null
    private var mPhotosZoomListener: MyRecyclerView.MyZoomListener? = null
    private var mPhotosLastMediaHandler = Handler()

    private var mStoredPhotosAnimateGifs = true
    private var mStoredPhotosCropThumbnails = true
    private var mStoredPhotosScrollHorizontally = true
    private var mStoredPhotosShowFileTypes = true
    private var mStoredPhotosRoundedCorners = false
    private var mStoredPhotosMarkFavoriteItems = true
    private var mStoredPhotosTextColor = 0
    private var mStoredPhotosPrimaryColor = 0
    private var mStoredPhotosThumbnailSpacing = 0

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)

        if (savedInstanceState == null) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            config.tempSkipRecycleBin = false
            removeTempFolder()
            checkRecycleBinItems()
            startNewPhotoFetcher()
        }

        mIsPickImageIntent = isPickImageIntent(intent)
        mIsPickVideoIntent = isPickVideoIntent(intent)
        mIsGetImageContentIntent = isGetImageContentIntent(intent)
        mIsGetVideoContentIntent = isGetVideoContentIntent(intent)
        mIsGetAnyContentIntent = isGetAnyContentIntent(intent)
        mIsSetWallpaperIntent = isSetWallpaperIntent(intent)
        mAllowPickingMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        mIsThirdPartyIntent = mIsPickImageIntent
                || mIsPickVideoIntent
                || mIsGetImageContentIntent
                || mIsGetVideoContentIntent
                || mIsGetAnyContentIntent
                || mIsSetWallpaperIntent

        // third party intents always start at the folders view, the all-content view is opened from there
        mShowingPhotos = !mIsThirdPartyIntent && config.showAll

        setupOptionsMenu()
        setupPhotosOptionsMenu()
        setupBottomNavigation()
        refreshMenuItems()
        refreshPhotosMenuItems()

        // the top inset is applied to the app bars themselves, so that the app bar can scroll away
        // completely while the content stays visible behind the transparent status bar. The bottom
        // navigation takes care of the bottom system insets and hides itself while the keyboard is shown
        setupEdgeToEdge()
        setupBottomNavigationImeHandling()

        binding.directoriesRefreshLayout.setOnRefreshListener { getDirectories() }
        binding.photosRefreshLayout.setOnRefreshListener { getPhotos() }
        binding.photosEmptyTextPlaceholder2.setOnClickListener {
            showPhotosFilterMediaDialog()
        }

        binding.photosCoordinator.beVisibleIf(mShowingPhotos)
        binding.directoriesCoordinator.beVisibleIf(!mShowingPhotos)

        storeStateVariables()
        checkWhatsNewDialog()
        setupLatestMediaId()

        if (!config.wereFavoritesPinned) {
            config.addPinnedFolders(hashSetOf(FAVORITES))
            config.wereFavoritesPinned = true
        }

        if (!config.wasRecycleBinPinned) {
            config.addPinnedFolders(hashSetOf(RECYCLE_BIN))
            config.wasRecycleBinPinned = true
            config.saveFolderGrouping(SHOW_ALL, GROUP_BY_DATE_TAKEN_DAILY or GROUP_DESCENDING)
        }

        if (!config.wasSVGShowingHandled) {
            config.wasSVGShowingHandled = true
            if (config.filterMedia and TYPE_SVGS == 0) {
                config.filterMedia += TYPE_SVGS
            }
        }

        if (!config.wasSortingByNumericValueAdded) {
            config.wasSortingByNumericValueAdded = true
            config.sorting = config.sorting or SORT_USE_NUMERIC_VALUE
        }

        updateWidgets()
        registerFileUpdateListener()

        binding.directoriesSwitchSearching.setOnClickListener {
            launchSearchActivity()
        }

        // just request the permission, tryLoadGallery will then trigger in onResume
        handleMediaPermissions()
    }

    private fun setupBottomNavigationImeHandling() {
        if (isRPlus()) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { _, insets ->
                binding.bottomNavigation.beVisibleIf(!insets.isVisible(Type.ime()))
                insets
            }
        } else {
            // IME insets are not dispatched below Android 11, detect the keyboard by the visible display frame
            val displayHeight = resources.displayMetrics.heightPixels
            val decorView = window.decorView
            decorView.viewTreeObserver.addOnGlobalLayoutListener {
                val visibleFrame = Rect()
                decorView.getWindowVisibleDisplayFrame(visibleFrame)
                val imeHeight = displayHeight - visibleFrame.bottom
                binding.bottomNavigation.beVisibleIf(imeHeight < displayHeight * IME_VISIBILITY_THRESHOLD)
            }
        }
    }

    private fun handleMediaPermissions(callback: (() -> Unit)? = null) {
        requestMediaPermissions(enableRationale = true) {
            callback?.invoke()
            if (isRPlus() && !mWasMediaManagementPromptShown) {
                mWasMediaManagementPromptShown = true
                handleMediaManagementPrompt { }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        updateMenuColors()
        config.isThirdPartyIntent = false
        mDateFormat = config.dateFormat
        mTimeFormat = getTimeFormat()

        refreshMenuItems()
        refreshPhotosMenuItems()

        if (mShowingPhotos) {
            updatePhotosTab()
        }

        if (mStoredAnimateGifs != config.animateGifs) {
            getRecyclerAdapter()?.updateAnimateGifs(config.animateGifs)
        }

        if (mStoredCropThumbnails != config.cropThumbnails) {
            getRecyclerAdapter()?.updateCropThumbnails(config.cropThumbnails)
        }

        if (mStoredScrollHorizontally != config.scrollHorizontally) {
            mLoadedInitialPhotos = false
            binding.directoriesGrid.adapter = null
            binding.mainMenu.setCollapsibleOnScroll(!config.scrollHorizontally)
            binding.photosMenu.setCollapsibleOnScroll(!config.scrollHorizontally)
            getDirectories()
        }

        if (mStoredTextColor != getProperTextColor()) {
            getRecyclerAdapter()?.updateTextColor(getProperTextColor())
        }

        val primaryColor = getProperPrimaryColor()
        if (mStoredPrimaryColor != primaryColor) {
            getRecyclerAdapter()?.updatePrimaryColor()
        }

        val styleString =
            "${config.folderStyle}${config.showFolderMediaCount}${config.limitFolderTitle}"
        if (mStoredStyleString != styleString) {
            setupAdapter(mDirs, forceRecreate = true)
        }

        binding.directoriesFastscroller.updateColors(primaryColor)
        binding.directoriesRefreshLayout.isEnabled = config.enablePullToRefresh
        getRecyclerAdapter()?.apply {
            dateFormat = config.dateFormat
            timeFormat = getTimeFormat()
        }

        binding.directoriesEmptyPlaceholder.setTextColor(getProperTextColor())
        binding.directoriesEmptyPlaceholder2.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.underlineText()
        binding.directoriesEmptyPlaceholder2.bringToFront()

        val activeMenu = if (mShowingPhotos) binding.photosMenu else binding.mainMenu
        if (!activeMenu.isSearchOpen) {
            refreshActiveMenu()
            tryLoadGallery()
        }

        if (config.searchAllFilesByDefault) {
            binding.mainMenu.updateHintText(getString(org.fossify.commons.R.string.search_files))
        } else {
            binding.mainMenu.updateHintText(getString(org.fossify.commons.R.string.search_folders))
        }
    }

    override fun onPause() {
        super.onPause()
        binding.directoriesRefreshLayout.isRefreshing = false
        binding.photosRefreshLayout.isRefreshing = false
        mIsGettingDirs = false
        mIsGettingPhotos = false
        storeStateVariables()
        mLastMediaHandler.removeCallbacksAndMessages(null)
        mPhotosLastMediaHandler.removeCallbacksAndMessages(null)

        if (mPhotos.isNotEmpty()) {
            mCurrPhotosAsyncTask?.stopFetching()
        }
    }

    override fun onStop() {
        super.onStop()

        if (config.temporarilyShowHidden || config.tempSkipDeleteConfirmation || config.temporarilyShowExcluded) {
            mTempShowHiddenHandler.postDelayed({
                config.temporarilyShowHidden = false
                config.temporarilyShowExcluded = false
                config.tempSkipDeleteConfirmation = false
                config.tempSkipRecycleBin = false
            }, SHOW_TEMP_HIDDEN_DURATION)
        } else {
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            config.tempSkipRecycleBin = false
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
            mPhotosLastMediaHandler.removeCallbacksAndMessages(null)
            mCurrPhotosAsyncTask?.stopFetching()
            removeTempFolder()
            unregisterFileUpdateListener()

            if (!config.showAll) {
                mLastMediaFetcher?.shouldStop = true
                GalleryDatabase.destroyInstance()
            }
        }
    }

    override fun onBackPressedCompat(): Boolean {
        if (mShowingPhotos) {
            if (binding.photosMenu.isSearchOpen) {
                binding.photosMenu.closeSearch()
                return true
            }

            showTab(showPhotos = false)
            return true
        }

        return if (binding.mainMenu.isSearchOpen) {
            binding.mainMenu.closeSearch()
            true
        } else if (config.groupDirectSubfolders) {
            if (mCurrentPathPrefix.isEmpty()) {
                appLockManager.lock()
                false
            } else {
                mOpenedSubfolders.removeAt(mOpenedSubfolders.lastIndex)
                mCurrentPathPrefix = mOpenedSubfolders.last()
                setupAdapter(mDirs)
                true
            }
        } else {
            appLockManager.lock()
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_MEDIA && resultData != null) {
                val resultIntent = Intent()
                var resultUri: Uri? = null
                if (mIsThirdPartyIntent) {
                    when {
                        intent.extras?.containsKey(MediaStore.EXTRA_OUTPUT) == true
                                && intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0 -> {
                            resultUri = fillExtraOutput(resultData)
                        }

                        resultData.extras?.containsKey(PICKED_PATHS) == true -> {
                            fillPickedPaths(resultData, resultIntent)
                        }

                        else -> fillIntentPath(resultData, resultIntent)
                    }
                }

                if (resultUri != null) {
                    resultIntent.data = resultUri
                    resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            } else if (requestCode == PICK_WALLPAPER) {
                setResult(RESULT_OK)
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun refreshMenuItems() {
        if (!mIsThirdPartyIntent) {
            binding.mainMenu.requireToolbar().menu.apply {
                findItem(R.id.column_count).isVisible = config.viewTypeFolders == VIEW_TYPE_GRID
                findItem(R.id.set_as_default_folder).isVisible = !config.defaultFolder.isEmpty()
                findItem(R.id.open_recycle_bin).isVisible =
                    config.useRecycleBin && !config.showRecycleBinAtFolders
                findItem(R.id.more_apps_from_us).isVisible =
                    resources.getBoolean(org.fossify.commons.R.bool.is_google_play_build)
            }
        }

        binding.mainMenu.requireToolbar().menu.apply {
            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible =
                (!isRPlus() || isExternalStorageManager()) && config.temporarilyShowHidden

            findItem(R.id.temporarily_show_excluded).isVisible = !config.temporarilyShowExcluded
            findItem(R.id.stop_showing_excluded).isVisible = config.temporarilyShowExcluded
        }
    }

    private fun setupOptionsMenu() {
        val menuId = if (mIsThirdPartyIntent) {
            R.menu.menu_main_intent
        } else {
            R.menu.menu_main
        }

        binding.mainMenu.requireToolbar().inflateMenu(menuId)
        binding.mainMenu.setCollapsibleOnScroll(!config.scrollHorizontally)
        binding.mainMenu.setupMenu()

        binding.mainMenu.onSearchOpenListener = {
            if (config.searchAllFilesByDefault) {
                launchSearchActivity()
            }
        }

        binding.mainMenu.onSearchTextChangedListener = { text ->
            setupAdapter(mDirsIgnoringSearch, text)
            binding.directoriesRefreshLayout.isEnabled =
                text.isEmpty() && config.enablePullToRefresh
            binding.directoriesSwitchSearching.beVisibleIf(text.isNotEmpty())
        }

        binding.mainMenu.requireToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.filter -> showFilterMediaDialog()
                R.id.open_camera -> launchCamera()
                R.id.change_view_type -> changeViewType()
                R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                R.id.temporarily_show_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.stop_showing_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.create_new_folder -> createNewFolder()
                R.id.open_recycle_bin -> openRecycleBin()
                R.id.column_count -> changeColumnCount()
                R.id.set_as_default_folder -> setAsDefaultFolder()
                R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun updateMenuColors() {
        binding.mainMenu.updateColors()
        binding.photosMenu.updateColors()
    }

    private fun getRecyclerAdapter() = binding.directoriesGrid.adapter as? DirectoryAdapter

    private fun storeStateVariables() {
        mStoredTextColor = getProperTextColor()
        mStoredPrimaryColor = getProperPrimaryColor()
        mStoredPhotosTextColor = mStoredTextColor
        mStoredPhotosPrimaryColor = mStoredPrimaryColor
        config.apply {
            mStoredAnimateGifs = animateGifs
            mStoredCropThumbnails = cropThumbnails
            mStoredScrollHorizontally = scrollHorizontally
            mStoredStyleString = "$folderStyle$showFolderMediaCount$limitFolderTitle"

            mStoredPhotosAnimateGifs = animateGifs
            mStoredPhotosCropThumbnails = cropThumbnails
            mStoredPhotosScrollHorizontally = scrollHorizontally
            mStoredPhotosShowFileTypes = showThumbnailFileTypes
            mStoredPhotosMarkFavoriteItems = markFavoriteItems
            mStoredPhotosThumbnailSpacing = thumbnailSpacing
            mStoredPhotosRoundedCorners = fileRoundedCorners
        }
    }

    private fun refreshActiveMenu() {
        if (mShowingPhotos) {
            refreshPhotosMenuItems()
        } else {
            refreshMenuItems()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId =
            if (mShowingPhotos) R.id.nav_photos else R.id.nav_albums

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_photos -> {
                    showTab(showPhotos = true)
                    true
                }

                R.id.nav_albums -> {
                    showTab(showPhotos = false)
                    true
                }

                else -> false
            }
        }
    }

    private fun showTab(showPhotos: Boolean) {
        if (mIsThirdPartyIntent) {
            // while picking files from a third party app, the all-content view is a separate activity
            if (showPhotos) {
                binding.bottomNavigation.selectedItemId = R.id.nav_albums
                showAllMedia()
            }

            return
        }

        if (mShowingPhotos == showPhotos) {
            return
        }

        mShowingPhotos = showPhotos
        config.showAll = showPhotos
        hideKeyboard()

        binding.bottomNavigation.selectedItemId =
            if (showPhotos) R.id.nav_photos else R.id.nav_albums
        binding.photosCoordinator.beVisibleIf(showPhotos)
        binding.directoriesCoordinator.beVisibleIf(!showPhotos)

        if (showPhotos) {
            refreshPhotosMenuItems()
            getPhotos()
        } else {
            refreshMenuItems()
            getDirectories()
        }
    }

    private fun updatePhotosTab() {
        val photosAdapter = getPhotosAdapter()

        if (mStoredPhotosAnimateGifs != config.animateGifs) {
            photosAdapter?.updateAnimateGifs(config.animateGifs)
        }

        if (mStoredPhotosCropThumbnails != config.cropThumbnails) {
            photosAdapter?.updateCropThumbnails(config.cropThumbnails)
        }

        if (mStoredPhotosScrollHorizontally != config.scrollHorizontally) {
            mPhotosLoadedInitial = false
            binding.photosGrid.adapter = null
        }

        if (mStoredPhotosShowFileTypes != config.showThumbnailFileTypes) {
            photosAdapter?.updateShowFileTypes(config.showThumbnailFileTypes)
        }

        if (mStoredPhotosTextColor != getProperTextColor()) {
            photosAdapter?.updateTextColor(getProperTextColor())
        }

        if (mStoredPhotosPrimaryColor != getProperPrimaryColor()) {
            photosAdapter?.updatePrimaryColor()
        }

        if (
            mStoredPhotosThumbnailSpacing != config.thumbnailSpacing
            || mStoredPhotosRoundedCorners != config.fileRoundedCorners
            || mStoredPhotosMarkFavoriteItems != config.markFavoriteItems
        ) {
            binding.photosGrid.adapter = null
            setupPhotosAdapter()
        }

        refreshPhotosMenuItems()
        binding.photosFastscroller.updateColors(getProperPrimaryColor())
        binding.photosRefreshLayout.isEnabled = config.enablePullToRefresh
        photosAdapter?.apply {
            dateFormat = config.dateFormat
            timeFormat = getTimeFormat()
        }

        binding.photosLoadingIndicator.setIndicatorColor(getProperPrimaryColor())
        binding.photosEmptyTextPlaceholder.setTextColor(getProperTextColor())
        binding.photosEmptyTextPlaceholder2.setTextColor(getProperPrimaryColor())
        binding.photosEmptyTextPlaceholder2.bringToFront()
    }

    private fun setupPhotosOptionsMenu() {
        binding.photosMenu.requireToolbar().inflateMenu(R.menu.menu_media)
        binding.photosMenu.setCollapsibleOnScroll(!config.scrollHorizontally)
        binding.photosMenu.setupMenu()
        binding.photosMenu.updateHintText(getString(org.fossify.commons.R.string.search_files))

        binding.photosMenu.onSearchTextChangedListener = { text ->
            mPhotosLastSearchedText = text
            searchPhotos(text)
            binding.photosRefreshLayout.isEnabled =
                text.isEmpty() && config.enablePullToRefresh
        }

        binding.photosMenu.requireToolbar().setOnMenuItemClickListener { menuItem ->
            handlePhotosMenuItem(menuItem.itemId)
        }
    }

    private fun handlePhotosMenuItem(itemId: Int): Boolean {
        when (itemId) {
            R.id.temporarily_show_hidden,
            R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()

            R.id.sort, R.id.filter, R.id.group, R.id.change_view_type -> showPhotosDialog(itemId)
            R.id.toggle_filename -> togglePhotosFilenameVisibility()
            R.id.open_camera -> launchCamera()
            R.id.open_recycle_bin -> openRecycleBin()
            R.id.column_count -> changePhotosColumnCount()
            R.id.slideshow -> startPhotosSlideshow()
            R.id.set_as_default_folder -> unsetDefaultFolder()
            R.id.settings -> launchSettings()
            R.id.about -> launchAbout()
            else -> return false
        }

        return true
    }

    private fun showPhotosDialog(itemId: Int) {
        when (itemId) {
            R.id.sort -> showPhotosSortingDialog()
            R.id.filter -> showPhotosFilterMediaDialog()
            R.id.group -> showPhotosGroupByDialog()
            R.id.change_view_type -> changePhotosViewType()
        }
    }

    private fun refreshPhotosMenuItems() {
        binding.photosMenu.requireToolbar().menu.apply {
            // the folders view is reachable through the bottom navigation now
            findItem(R.id.folder_view).isVisible = false
            findItem(R.id.group).isVisible = !config.scrollHorizontally
            findItem(R.id.open_camera).isVisible = true
            findItem(R.id.about).isVisible = true
            findItem(R.id.create_new_folder).isVisible = false
            findItem(R.id.empty_recycle_bin).isVisible = false
            findItem(R.id.empty_disable_recycle_bin).isVisible = false
            findItem(R.id.restore_all_files).isVisible = false
            findItem(R.id.unset_as_default_folder).isVisible = false
            findItem(R.id.set_as_default_folder).isVisible = config.defaultFolder.isNotEmpty()
            findItem(R.id.open_recycle_bin).isVisible = config.useRecycleBin

            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible =
                (!isRPlus() || isExternalStorageManager()) && config.temporarilyShowHidden

            val viewType = config.getFolderViewType(SHOW_ALL)
            findItem(R.id.column_count).isVisible = viewType == VIEW_TYPE_GRID
            findItem(R.id.toggle_filename).isVisible = viewType == VIEW_TYPE_GRID
        }
    }

    private fun unsetDefaultFolder() {
        config.defaultFolder = ""
        refreshPhotosMenuItems()
        refreshMenuItems()
    }

    private fun changePhotosViewType() {
        ChangeViewTypeDialog(this, false, SHOW_ALL) {
            refreshPhotosMenuItems()
            setupPhotosLayoutManager()
            binding.photosGrid.adapter = null
            setupPhotosAdapter()
        }
    }

    private fun showPhotosSortingDialog() {
        ChangeSortingDialog(this, false, true, SHOW_ALL) {
            mPhotosLoadedInitial = false
            binding.photosGrid.adapter = null
            getPhotos()
        }
    }

    private fun showPhotosGroupByDialog() {
        ChangeGroupingDialog(this, SHOW_ALL) {
            mPhotosLoadedInitial = false
            binding.photosGrid.adapter = null
            getPhotos()
        }
    }

    private fun showPhotosFilterMediaDialog() {
        FilterMediaDialog(this) {
            mPhotosLoadedInitial = false
            binding.photosRefreshLayout.isRefreshing = true
            binding.photosGrid.adapter = null
            getPhotos()
        }
    }

    private fun togglePhotosFilenameVisibility() {
        config.displayFileNames = !config.displayFileNames
        getPhotosAdapter()?.updateDisplayFilenames(config.displayFileNames)
    }

    private fun startPhotosSlideshow() {
        if (mPhotos.isEmpty()) {
            return
        }

        hideKeyboard()
        Intent(this, ViewPagerActivity::class.java).apply {
            val path = mPhotos.firstOrNull { it is Medium }?.let { (it as Medium).path } ?: return
            putExtra(SKIP_AUTHENTICATION, true)
            putExtra(PATH, path)
            putExtra(SHOW_ALL, true)
            putExtra(SLIDESHOW_START_ON_ENTER, true)
            startActivity(this)
        }
    }

    private fun getPhotos() {
        if (mIsGettingPhotos) {
            return
        }

        mIsGettingPhotos = true
        if (mPhotosLoadedInitial) {
            startPhotosAsyncTask()
        } else {
            getCachedMedia("") {
                if (it.isEmpty()) {
                    runOnUiThread {
                        binding.photosRefreshLayout.isRefreshing = true
                    }
                } else {
                    gotPhotos(it, true)
                }

                startPhotosAsyncTask()
            }
        }

        mPhotosLoadedInitial = true
    }

    private fun startPhotosAsyncTask() {
        mCurrPhotosAsyncTask?.stopFetching()
        mCurrPhotosAsyncTask = GetMediaAsynctask(
            context = applicationContext,
            mPath = "",
            showAll = true
        ) {
            ensureBackgroundThread {
                val oldMedia = mPhotos.clone() as ArrayList<ThumbnailItem>
                val newMedia = it
                try {
                    gotPhotos(newMedia, false)

                    // remove cached files that are no longer valid for whatever reason
                    val newPaths = newMedia.mapNotNull { item -> item as? Medium }.map { it.path }
                    oldMedia
                        .mapNotNull { item -> item as? Medium }
                        .filter { !newPaths.contains(it.path) }
                        .forEach { mediaDB.deleteMediumPath(it.path) }
                } catch (ignored: Exception) {
                }
            }
        }

        mCurrPhotosAsyncTask!!.execute()
    }

    private fun gotPhotos(media: ArrayList<ThumbnailItem>, isFromCache: Boolean) {
        mIsGettingPhotos = false
        checkPhotosLastMediaChanged()
        mPhotos = media

        runOnUiThread {
            binding.photosLoadingIndicator.hide()
            binding.photosRefreshLayout.isRefreshing = false
            binding.photosEmptyTextPlaceholder.beVisibleIf(media.isEmpty() && !isFromCache)
            binding.photosEmptyTextPlaceholder2.beVisibleIf(media.isEmpty() && !isFromCache)

            if (binding.photosEmptyTextPlaceholder.isVisible()) {
                binding.photosEmptyTextPlaceholder.text = getString(R.string.no_media_with_filters)
            }

            binding.photosFastscroller.beVisibleIf(binding.photosEmptyTextPlaceholder.isGone())
            setupPhotosAdapter()
        }

        mPhotosLatestMediaId = getLatestMediaId()
        mPhotosLatestMediaDateId = getLatestMediaByDateId()

        if (!isFromCache) {
            val mediaToInsert = mPhotos
                .filter { it is Medium && it.deletedTS == 0L }
                .map { it as Medium }
            Thread {
                try {
                    mediaDB.insertAll(mediaToInsert)
                } catch (ignored: Exception) {
                }
            }.start()
        }
    }

    private fun getPhotosAdapter() = binding.photosGrid.adapter as? MediaAdapter

    private fun setupPhotosAdapter() {
        val currAdapter = binding.photosGrid.adapter
        if (currAdapter == null) {
            initPhotosZoomListener()
            MediaAdapter(
                activity = this,
                media = mPhotos.clone() as ArrayList<ThumbnailItem>,
                listener = this,
                isAGetIntent = false,
                allowMultiplePicks = false,
                path = SHOW_ALL,
                recyclerView = binding.photosGrid
            ) {
                if (it is Medium && !isFinishing) {
                    photosItemClicked(it.path)
                }
            }.apply {
                setupZoomListener(mPhotosZoomListener)
                binding.photosGrid.adapter = this
            }

            if (config.getFolderViewType(SHOW_ALL) == VIEW_TYPE_LIST && areSystemAnimationsEnabled) {
                binding.photosGrid.scheduleLayoutAnimation()
            }

            setupPhotosLayoutManager()
            handlePhotosGridSpacing()
        } else if (mPhotosLastSearchedText.isEmpty()) {
            (currAdapter as MediaAdapter).updateMedia(mPhotos)
            handlePhotosGridSpacing()
        } else {
            searchPhotos(mPhotosLastSearchedText)
        }

        setupPhotosScrollDirection()
    }

    private fun setupPhotosScrollDirection() {
        val scrollHorizontally =
            config.scrollHorizontally && config.getFolderViewType(SHOW_ALL) == VIEW_TYPE_GRID
        binding.photosFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun setupPhotosLayoutManager() {
        if (config.getFolderViewType(SHOW_ALL) == VIEW_TYPE_GRID) {
            setupPhotosGridLayoutManager()
        } else {
            setupPhotosListLayoutManager()
        }
    }

    private fun setupPhotosGridLayoutManager() {
        val layoutManager = binding.photosGrid.layoutManager as MyGridLayoutManager
        if (config.scrollHorizontally) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.photosRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            layoutManager.orientation = RecyclerView.VERTICAL
            binding.photosRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        layoutManager.spanCount = config.mediaColumnCnt
        val adapter = getPhotosAdapter()
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.isASectionTitle(position) == true) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun setupPhotosListLayoutManager() {
        val layoutManager = binding.photosGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.photosRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mPhotosZoomListener = null
    }

    private fun handlePhotosGridSpacing(media: ArrayList<ThumbnailItem> = mPhotos) {
        if (config.getFolderViewType(SHOW_ALL) != VIEW_TYPE_GRID) {
            return
        }

        val spanCount = config.mediaColumnCnt
        val spacing = config.thumbnailSpacing
        val useGridPosition = media.firstOrNull() is ThumbnailSection

        var currentGridDecoration: GridSpacingItemDecoration? = null
        if (binding.photosGrid.itemDecorationCount > 0) {
            currentGridDecoration =
                binding.photosGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
            currentGridDecoration.items = media
        }

        val newGridDecoration = GridSpacingItemDecoration(
            spanCount = spanCount,
            spacing = spacing,
            isScrollingHorizontally = config.scrollHorizontally,
            addSideSpacing = config.fileRoundedCorners,
            items = media,
            useGridPosition = useGridPosition
        )

        if (currentGridDecoration.toString() != newGridDecoration.toString()) {
            if (currentGridDecoration != null) {
                binding.photosGrid.removeItemDecoration(currentGridDecoration)
            }

            binding.photosGrid.addItemDecoration(newGridDecoration)
        }
    }

    private fun initPhotosZoomListener() {
        if (config.getFolderViewType(SHOW_ALL) == VIEW_TYPE_GRID) {
            val layoutManager = binding.photosGrid.layoutManager as MyGridLayoutManager
            mPhotosZoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reducePhotosColumnCount()
                        getPhotosAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increasePhotosColumnCount()
                        getPhotosAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            mPhotosZoomListener = null
        }
    }

    private fun changePhotosColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(
                RadioItem(
                    id = i,
                    title = resources.getQuantityString(
                        org.fossify.commons.R.plurals.column_counts, i, i
                    )
                )
            )
        }

        val currentColumnCount = (binding.photosGrid.layoutManager as MyGridLayoutManager).spanCount
        RadioGroupDialog(this, items, currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.mediaColumnCnt = newColumnCount
                photosColumnCountChanged()
            }
        }
    }

    private fun increasePhotosColumnCount() {
        config.mediaColumnCnt += 1
        photosColumnCountChanged()
    }

    private fun reducePhotosColumnCount() {
        config.mediaColumnCnt -= 1
        photosColumnCountChanged()
    }

    private fun photosColumnCountChanged() {
        (binding.photosGrid.layoutManager as MyGridLayoutManager).spanCount = config.mediaColumnCnt
        handlePhotosGridSpacing()
        refreshPhotosMenuItems()
        getPhotosAdapter()?.apply {
            notifyItemRangeChanged(0, media.size)
        }
    }

    private fun searchPhotos(text: String) {
        ensureBackgroundThread {
            try {
                val filtered = mPhotos
                    .filter { it is Medium && it.name.contains(text, true) } as ArrayList
                filtered.sortBy { it is Medium && !it.name.startsWith(text, true) }
                val grouped = MediaFetcher(applicationContext).groupMedia(
                    media = filtered as ArrayList<Medium>, path = SHOW_ALL
                )
                runOnUiThread {
                    if (grouped.isEmpty()) {
                        binding.photosEmptyTextPlaceholder.text =
                            getString(org.fossify.commons.R.string.no_items_found)
                        binding.photosEmptyTextPlaceholder.beVisible()
                        binding.photosFastscroller.beGone()
                    } else {
                        binding.photosEmptyTextPlaceholder.beGone()
                        binding.photosFastscroller.beVisible()
                    }

                    handlePhotosGridSpacing(grouped)
                    getPhotosAdapter()?.updateMedia(grouped)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun photosItemClicked(path: String) {
        hideKeyboard()
        mPhotosWereVisibleInFullscreen = true

        if (!path.isVideoFast()) {
            openPhotosInViewPager(path)
            return
        }

        when (config.videoPlayerType) {
            VIDEO_PLAYER_SYSTEM -> openPath(path = path, forceChooser = false)
            VIDEO_PLAYER_APP -> if (config.gestureVideoPlayer) {
                launchGesturePlayer(path)
            } else {
                openPhotosInViewPager(path)
            }

            else -> openPhotosInViewPager(path) // unreachable by design
        }
    }

    private fun openPhotosInViewPager(path: String) {
        Intent(this, ViewPagerActivity::class.java).apply {
            putExtra(SKIP_AUTHENTICATION, true)
            putExtra(PATH, path)
            putExtra(SHOW_ALL, true)
            putExtra(IS_FROM_GALLERY, true)
            startActivity(this)
        }
    }

    private fun checkPhotosLastMediaChanged() {
        if (isDestroyed || config.getFolderSorting(SHOW_ALL) and SORT_BY_RANDOM != 0) {
            return
        }

        mPhotosLastMediaHandler.removeCallbacksAndMessages(null)
        mPhotosLastMediaHandler.postDelayed({
            ensureBackgroundThread {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mPhotosLatestMediaId != mediaId || mPhotosLatestMediaDateId != mediaDateId) {
                    mPhotosLatestMediaId = mediaId
                    mPhotosLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getPhotos()
                    }
                } else {
                    checkPhotosLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }

    private fun startNewPhotoFetcher() {
        val photoFetcher = NewPhotoFetcher()
        if (!photoFetcher.isScheduled(applicationContext)) {
            photoFetcher.scheduleJob(applicationContext)
        }
    }

    private fun removeTempFolder() {
        if (config.tempFolderPath.isNotEmpty()) {
            val newFolder = File(config.tempFolderPath)
            if (getDoesFilePathExist(newFolder.absolutePath) && newFolder.isDirectory) {
                if (
                    newFolder.getProperSize(true) == 0L
                    && newFolder.getFileCount(true) == 0
                    && newFolder.list()?.isEmpty() == true
                ) {
                    toast(
                        String.format(
                            getString(org.fossify.commons.R.string.deleting_folder),
                            config.tempFolderPath
                        ), Toast.LENGTH_LONG
                    )
                    tryDeleteFileDirItem(newFolder.toFileDirItem(applicationContext), true, true)
                }
            }
            config.tempFolderPath = ""
        }
    }

    private fun checkOTGPath() {
        ensureBackgroundThread {
            if (!config.wasOTGHandled && hasPermission(getPermissionToRequest()) && hasOTGConnected() && config.OTGPath.isEmpty()) {
                getStorageDirectories().firstOrNull {
                    it.trimEnd('/') != internalStoragePath
                            && it.trimEnd('/') != sdCardPath
                }?.apply {
                    config.wasOTGHandled = true
                    val otgPath = trimEnd('/')
                    config.OTGPath = otgPath
                    config.addIncludedFolder(otgPath)
                }
            }
        }
    }

    private fun checkDefaultSpamFolders() {
        if (!config.spamFoldersChecked) {
            val spamFolders = arrayListOf(
                "/storage/emulated/0/Android/data/com.facebook.orca/files/stickers"
            )

            val OTGPath = config.OTGPath
            spamFolders.forEach {
                if (getDoesFilePathExist(it, OTGPath)) {
                    config.addExcludedFolder(it)
                }
            }
            config.spamFoldersChecked = true
        }
    }

    private fun tryLoadGallery() {
        // avoid calling anything right after granting the permission, it will be called from onResume()
        val wasMissingPermission =
            config.appRunCount == 1 && !hasAllPermissions(getPermissionsToRequest())
        handleMediaPermissions {
            if (wasMissingPermission) {
                return@handleMediaPermissions
            }

            if (!mWasDefaultFolderChecked) {
                // do not jump into the default folder while the photos are being displayed
                if (!mShowingPhotos) {
                    openDefaultFolder()
                }

                mWasDefaultFolderChecked = true
            }

            checkOTGPath()
            checkDefaultSpamFolders()

            if (mShowingPhotos) {
                if (mShowPhotosLoadingIndicator) {
                    binding.photosLoadingIndicator.show()
                    mShowPhotosLoadingIndicator = false
                }

                val isRandomSorting = config.getFolderSorting(SHOW_ALL) and SORT_BY_RANDOM != 0
                if (mPhotos.isEmpty() || !isRandomSorting || !mPhotosWereVisibleInFullscreen) {
                    mPhotosWereVisibleInFullscreen = false
                    getPhotos()
                }
            } else {
                getDirectories()
            }

            setupLayoutManager()
        }
    }

    private fun getDirectories() {
        if (mIsGettingDirs) {
            return
        }

        mShouldStopFetching = true
        mIsGettingDirs = true
        val getImages = mIsPickImageIntent || mIsGetImageContentIntent
        val getVideos = mIsPickVideoIntent || mIsGetVideoContentIntent

        getCachedDirectories(getVideos && !getImages, getImages && !getVideos) {
            gotDirectories(addTempFolderIfNeeded(it))
        }
    }

    private fun launchSearchActivity() {
        hideKeyboard()
        Intent(this, SearchActivity::class.java).apply {
            startActivity(this)
        }

        binding.mainMenu.postDelayed({
            binding.mainMenu.closeSearch()
        }, 500)
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this, true, false) {
            binding.directoriesGrid.adapter = null
            if (config.directorySorting and SORT_BY_DATE_MODIFIED != 0 || config.directorySorting and SORT_BY_DATE_TAKEN != 0) {
                getDirectories()
            } else {
                ensureBackgroundThread {
                    gotDirectories(getCurrentlyDisplayedDirs())
                }
            }

            getRecyclerAdapter()?.directorySorting = config.directorySorting
        }
    }

    private fun showFilterMediaDialog() {
        FilterMediaDialog(this) {
            mShouldStopFetching = true
            binding.directoriesRefreshLayout.isRefreshing = true
            binding.directoriesGrid.adapter = null
            getDirectories()
        }
    }

    private fun showAllMedia() {
        config.showAll = true
        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, "")

            if (mIsThirdPartyIntent) {
                handleMediaIntent(this)
            } else {
                hideKeyboard()
                startActivity(this)
                finish()
            }
        }
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this, true) {
            refreshMenuItems()
            setupLayoutManager()
            binding.directoriesGrid.adapter = null
            setupAdapter(getRecyclerAdapter()?.dirs ?: mDirs)
        }
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            if (isRPlus() && !isExternalStorageManager()) {
                GrantAllFilesDialog(this)
            } else {
                handleHiddenFolderPasswordProtection {
                    toggleTemporarilyShowHidden(true)
                }
            }
        }
    }

    private fun toggleTemporarilyShowHidden(show: Boolean) {
        mLoadedInitialPhotos = false
        mPhotosLoadedInitial = false
        config.temporarilyShowHidden = show
        binding.directoriesGrid.adapter = null
        binding.photosGrid.adapter = null
        refreshActiveMenu()

        if (mShowingPhotos) {
            getPhotos()
        } else {
            getDirectories()
        }
    }

    private fun tryToggleTemporarilyShowExcluded() {
        if (config.temporarilyShowExcluded) {
            toggleTemporarilyShowExcluded(false)
        } else {
            handleExcludedFolderPasswordProtection {
                toggleTemporarilyShowExcluded(true)
            }
        }
    }

    private fun toggleTemporarilyShowExcluded(show: Boolean) {
        mLoadedInitialPhotos = false
        mPhotosLoadedInitial = false
        config.temporarilyShowExcluded = show
        binding.directoriesGrid.adapter = null
        binding.photosGrid.adapter = null
        refreshActiveMenu()

        if (mShowingPhotos) {
            getPhotos()
        } else {
            getDirectories()
        }
    }

    override fun deleteFolders(folders: ArrayList<File>) {
        val fileDirItems = folders
            .asSequence()
            .filter { it.isDirectory }
            .map { FileDirItem(it.absolutePath, it.name, true) }
            .toMutableList() as ArrayList<FileDirItem>

        when {
            fileDirItems.isEmpty() -> return
            fileDirItems.size == 1 -> {
                try {
                    toast(
                        String.format(
                            getString(org.fossify.commons.R.string.deleting_folder),
                            fileDirItems.first().name
                        )
                    )
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }

            else -> {
                val baseString = if (config.useRecycleBin && !config.tempSkipRecycleBin) {
                    org.fossify.commons.R.plurals.moving_items_into_bin
                } else {
                    org.fossify.commons.R.plurals.delete_items
                }

                toast(
                    msg = resources.getQuantityString(
                        baseString, fileDirItems.size, fileDirItems.size
                    )
                )
            }
        }

        val itemsToDelete = ArrayList<FileDirItem>()
        val filter = config.filterMedia
        val showHidden = config.shouldShowHidden
        fileDirItems.filter { it.isDirectory }.forEach {
            val files = File(it.path).listFiles()
            files?.filter {
                it.absolutePath.isMediaFile()
                        && (showHidden || !it.name.startsWith('.'))
                        && ((it.isImageFast() && filter and TYPE_IMAGES != 0)
                        || (it.isVideoFast() && filter and TYPE_VIDEOS != 0)
                        || (it.isGif() && filter and TYPE_GIFS != 0)
                        || (it.isRawFast() && filter and TYPE_RAWS != 0)
                        || (it.isSvg() && filter and TYPE_SVGS != 0))
            }?.mapTo(itemsToDelete) { it.toFileDirItem(applicationContext) }
        }

        if (config.useRecycleBin && !config.tempSkipRecycleBin) {
            val pathsToDelete = ArrayList<String>()
            itemsToDelete.mapTo(pathsToDelete) { it.path }

            movePathsInRecycleBin(pathsToDelete) {
                if (it) {
                    deleteFilteredFileDirItems(itemsToDelete, folders)
                } else {
                    toast(org.fossify.commons.R.string.unknown_error_occurred)
                }
            }
        } else {
            deleteFilteredFileDirItems(itemsToDelete, folders)
        }
    }

    private fun deleteFilteredFileDirItems(
        fileDirItems: ArrayList<FileDirItem>,
        folders: ArrayList<File>
    ) {
        val OTGPath = config.OTGPath
        deleteFiles(fileDirItems) {
            runOnUiThread {
                refreshItems()
            }

            ensureBackgroundThread {
                folders.filter { !getDoesFilePathExist(it.absolutePath, OTGPath) }.forEach {
                    directoryDB.deleteDirPath(it.absolutePath)
                }

                if (config.deleteEmptyFolders) {
                    folders.filter {
                        !it.absolutePath.isDownloadsFolder()
                                && it.isDirectory
                                && it.toFileDirItem(this).getProperFileCount(this, true) == 0
                    }
                        .forEach {
                            tryDeleteFileDirItem(it.toFileDirItem(this), true, true)
                        }
                }
            }
        }
    }

    private fun setupLayoutManager() {
        if (config.viewTypeFolders == VIEW_TYPE_GRID) {
            setupGridLayoutManager()
        } else {
            setupListLayoutManager()
        }

        (binding.directoriesRefreshLayout.layoutParams as RelativeLayout.LayoutParams)
            .addRule(RelativeLayout.BELOW, R.id.directories_switch_searching)
    }

    private fun setupGridLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        if (config.scrollHorizontally) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.directoriesRefreshLayout.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
        } else {
            layoutManager.orientation = RecyclerView.VERTICAL
            binding.directoriesRefreshLayout.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        }

        layoutManager.spanCount = config.dirColumnCnt
    }

    private fun setupListLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.directoriesRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mZoomListener = null
    }

    private fun initZoomListener() {
        if (config.viewTypeFolders == VIEW_TYPE_GRID) {
            val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
            mZoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reduceColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increaseColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            mZoomListener = null
        }
    }

    private fun createNewFolder() {
        FilePickerDialog(this, internalStoragePath, false, config.shouldShowHidden, false, true) {
            CreateNewFolderDialog(this, it) {
                config.tempFolderPath = it
                ensureBackgroundThread {
                    gotDirectories(addTempFolderIfNeeded(getCurrentlyDisplayedDirs()))
                }
            }
        }
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(
                RadioItem(
                    id = i,
                    title = resources.getQuantityString(
                        org.fossify.commons.R.plurals.column_counts, i, i
                    )
                )
            )
        }

        val currentColumnCount =
            (binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount
        RadioGroupDialog(this, items, currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.dirColumnCnt = newColumnCount
                columnCountChanged()
            }
        }
    }

    private fun increaseColumnCount() {
        config.dirColumnCnt += 1
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        config.dirColumnCnt -= 1
        columnCountChanged()
    }

    private fun columnCountChanged() {
        (binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount =
            config.dirColumnCnt
        refreshMenuItems()
        getRecyclerAdapter()?.apply {
            notifyItemRangeChanged(0, dirs.size)
        }
    }

    private fun isPickImageIntent(intent: Intent): Boolean {
        return isPickIntent(intent) && (hasImageContentData(intent) || isImageType(intent))
    }

    private fun isPickVideoIntent(intent: Intent): Boolean {
        return isPickIntent(intent) && (hasVideoContentData(intent) || isVideoType(intent))
    }

    private fun isPickIntent(intent: Intent): Boolean {
        return intent.action == Intent.ACTION_PICK
    }

    private fun isGetContentIntent(intent: Intent): Boolean {
        return intent.action == Intent.ACTION_GET_CONTENT && intent.type != null
    }

    private fun anyExtraMimeTypeStartingWith(intent: Intent, mimeTypePrefix: String): Boolean {
        return intent.getStringArrayExtra(Intent.EXTRA_MIME_TYPES)
            ?.any { it.startsWith(mimeTypePrefix) } == true
    }

    private fun isGetImageContentIntent(intent: Intent): Boolean {
        return isGetContentIntent(intent)
                && (intent.type!!.startsWith("image/")
                || intent.type == Images.Media.CONTENT_TYPE
                || anyExtraMimeTypeStartingWith(intent, "image/"))
    }

    private fun isGetVideoContentIntent(intent: Intent): Boolean {
        return isGetContentIntent(intent)
                && (intent.type!!.startsWith("video/")
                || intent.type == Video.Media.CONTENT_TYPE
                || anyExtraMimeTypeStartingWith(intent, "video/"))
    }

    private fun isGetAnyContentIntent(intent: Intent): Boolean {
        return isGetContentIntent(intent) && intent.type == "*/*"
    }

    private fun isSetWallpaperIntent(intent: Intent?): Boolean {
        return intent?.action == Intent.ACTION_SET_WALLPAPER
    }

    private fun hasImageContentData(intent: Intent): Boolean {
        return intent.data == Images.Media.EXTERNAL_CONTENT_URI
                || intent.data == Images.Media.INTERNAL_CONTENT_URI
    }

    private fun hasVideoContentData(intent: Intent): Boolean {
        return intent.data == Video.Media.EXTERNAL_CONTENT_URI
                || intent.data == Video.Media.INTERNAL_CONTENT_URI
    }

    private fun isImageType(intent: Intent): Boolean {
        return (intent.type?.startsWith("image/") == true
                || intent.type == Images.Media.CONTENT_TYPE)
    }

    private fun isVideoType(intent: Intent): Boolean {
        return (intent.type?.startsWith("video/") == true
                || intent.type == Video.Media.CONTENT_TYPE)
    }

    private fun fillExtraOutput(resultData: Intent): Uri? {
        val file = File(resultData.data!!.path!!)
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val output = intent.extras!!.get(MediaStore.EXTRA_OUTPUT) as Uri
            inputStream = FileInputStream(file)
            outputStream = contentResolver.openOutputStream(output)
            inputStream.copyTo(outputStream!!)
        } catch (e: SecurityException) {
            showErrorToast(e)
        } catch (ignored: FileNotFoundException) {
            return getFilePublicUri(file, BuildConfig.APPLICATION_ID)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return null
    }

    private fun fillPickedPaths(resultData: Intent, resultIntent: Intent) {
        val paths = resultData.extras!!.getStringArrayList(PICKED_PATHS)
        val uris = paths!!
            .map { getFilePublicUri(File(it), BuildConfig.APPLICATION_ID) } as ArrayList
        val clipData = ClipData(
            "Attachment",
            arrayOf("image/*", "video/*"),
            ClipData.Item(uris.removeAt(0))
        )

        uris.forEach {
            clipData.addItem(ClipData.Item(it))
        }

        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        resultIntent.clipData = clipData
    }

    private fun fillIntentPath(resultData: Intent, resultIntent: Intent) {
        val data = resultData.data
        val path = if (data.toString().startsWith("/")) data.toString() else data!!.path
        val uri = getFilePublicUri(File(path!!), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        resultIntent.setDataAndTypeAndNormalize(uri, type)
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun itemClicked(path: String) {
        handleLockedFolderOpening(path) { success ->
            if (success) {
                Intent(this, MediaActivity::class.java).apply {
                    putExtra(SKIP_AUTHENTICATION, true)
                    putExtra(DIRECTORY, path)
                    handleMediaIntent(this)
                }
            }
        }
    }

    private fun handleMediaIntent(intent: Intent) {
        hideKeyboard()
        intent.apply {
            if (mIsSetWallpaperIntent) {
                putExtra(SET_WALLPAPER_INTENT, true)
                startActivityForResult(this, PICK_WALLPAPER)
            } else {
                putExtra(GET_IMAGE_INTENT, mIsPickImageIntent || mIsGetImageContentIntent)
                putExtra(GET_VIDEO_INTENT, mIsPickVideoIntent || mIsGetVideoContentIntent)
                putExtra(GET_ANY_INTENT, mIsGetAnyContentIntent)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, mAllowPickingMultiple)
                startActivityForResult(this, PICK_MEDIA)
            }
        }
    }

    private fun gotDirectories(newDirs: ArrayList<Directory>) {
        mIsGettingDirs = false
        mShouldStopFetching = false

        // if hidden item showing is disabled but all Favorite items are hidden, hide the Favorites folder
        if (!config.shouldShowHidden) {
            val favoritesFolder = newDirs.firstOrNull { it.areFavorites() }
            if (
                favoritesFolder != null
                && favoritesFolder.tmb.getFilenameFromPath().startsWith('.')
            ) {
                newDirs.remove(favoritesFolder)
            }
        }

        val dirs = getSortedDirectories(newDirs)
        if (config.groupDirectSubfolders) {
            mDirs = dirs.clone() as ArrayList<Directory>
        }

        var isPlaceholderVisible = dirs.isEmpty()

        runOnUiThread {
            checkPlaceholderVisibility(dirs)
            setupAdapter(dirs.clone() as ArrayList<Directory>)
        }

        // cached folders have been loaded, recheck folders one by one starting with the first displayed
        mLastMediaFetcher?.shouldStop = true
        mLastMediaFetcher = MediaFetcher(applicationContext)
        val getImages = mIsPickImageIntent || mIsGetImageContentIntent
        val getVideos = mIsPickVideoIntent || mIsGetVideoContentIntent
        val getImagesOnly = getImages && !getVideos
        val getVideosOnly = getVideos && !getImages
        val favoritePaths = getFavoritePaths()
        val hiddenString = getString(R.string.hidden)
        val albumCovers = config.parseAlbumCovers()
        val includedFolders = config.includedFolders
        val noMediaFolders = getNoMediaFoldersSync()
        val tempFolderPath = config.tempFolderPath
        val getProperFileSize = config.directorySorting and SORT_BY_SIZE != 0
        val dirPathsToRemove = ArrayList<String>()
        val lastModifieds = mLastMediaFetcher!!.getLastModifieds()
        val dateTakens = mLastMediaFetcher!!.getDateTakens()

        if (
            config.showRecycleBinAtFolders
            && !config.showRecycleBinLast
            && !dirs.map { it.path }.contains(RECYCLE_BIN)
        ) {
            try {
                if (mediaDB.getDeletedMediaCount() > 0) {
                    val recycleBin = Directory().apply {
                        path = RECYCLE_BIN
                        name = getString(org.fossify.commons.R.string.recycle_bin)
                        location = LOCATION_INTERNAL
                    }

                    dirs.add(0, recycleBin)
                }
            } catch (ignored: Exception) {
            }
        }

        if (dirs.map { it.path }.contains(FAVORITES)) {
            if (mediaDB.getFavoritesCount() > 0) {
                val favorites = Directory().apply {
                    path = FAVORITES
                    name = getString(org.fossify.commons.R.string.favorites)
                    location = LOCATION_INTERNAL
                }

                dirs.add(0, favorites)
            }
        }

        // fetch files from MediaStore only, unless the app has the MANAGE_EXTERNAL_STORAGE permission on Android 11+
        val android11Files = mLastMediaFetcher?.getAndroid11FolderMedia(
            isPickImage = getImagesOnly,
            isPickVideo = getVideosOnly,
            favoritePaths = favoritePaths,
            getFavoritePathsOnly = false,
            getProperDateTaken = true,
            dateTakens = dateTakens
        )
        try {
            for (directory in dirs) {
                if (mShouldStopFetching || isDestroyed || isFinishing) {
                    return
                }

                val sorting = config.getFolderSorting(directory.path)
                val grouping = config.getFolderGrouping(directory.path)
                val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0
                        || sorting and SORT_BY_DATE_TAKEN != 0
                        || grouping and GROUP_BY_DATE_TAKEN_DAILY != 0
                        || grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

                val getProperLastModified =
                    config.directorySorting and SORT_BY_DATE_MODIFIED != 0
                            || sorting and SORT_BY_DATE_MODIFIED != 0
                            || grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0
                            || grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

                val curMedia = mLastMediaFetcher!!.getFilesFrom(
                    curPath = directory.path,
                    isPickImage = getImagesOnly,
                    isPickVideo = getVideosOnly,
                    getProperDateTaken = getProperDateTaken,
                    getProperLastModified = getProperLastModified,
                    getProperFileSize = getProperFileSize,
                    favoritePaths = favoritePaths,
                    getVideoDurations = false,
                    lastModifieds = lastModifieds,
                    dateTakens = dateTakens,
                    android11Files = android11Files
                )

                val newDir = if (curMedia.isEmpty()) {
                    if (directory.path != tempFolderPath) {
                        dirPathsToRemove.add(directory.path)
                    }
                    directory
                } else {
                    createDirectoryFromMedia(
                        path = directory.path,
                        curMedia = curMedia,
                        albumCovers = albumCovers,
                        hiddenString = hiddenString,
                        includedFolders = includedFolders,
                        getProperFileSize = getProperFileSize,
                        noMediaFolders = noMediaFolders
                    )
                }

                // we are looping through the already displayed folders looking for changes, do not do anything if nothing changed
                if (directory.copy(subfoldersCount = 0, subfoldersMediaCount = 0) == newDir) {
                    continue
                }

                directory.apply {
                    tmb = newDir.tmb
                    name = newDir.name
                    mediaCnt = newDir.mediaCnt
                    modified = newDir.modified
                    taken = newDir.taken
                    this@apply.size = newDir.size
                    types = newDir.types
                    sortValue = getDirectorySortingValue(curMedia, path, name, size, mediaCnt)
                }

                setupAdapter(dirs)

                // update directories and media files in the local db, delete invalid items. Intentionally creating a new thread
                updateDBDirectory(directory)
                if (!directory.isRecycleBin() && !directory.areFavorites()) {
                    Thread {
                        try {
                            mediaDB.insertAll(curMedia)
                        } catch (ignored: Exception) {
                        }
                    }.start()
                }

                if (!directory.isRecycleBin()) {
                    getCachedMedia(directory.path, getVideosOnly, getImagesOnly) {
                        val mediaToDelete = ArrayList<Medium>()
                        it.forEach {
                            if (!curMedia.contains(it)) {
                                val medium = it as? Medium
                                val path = medium?.path
                                if (path != null) {
                                    mediaToDelete.add(medium)
                                }
                            }
                        }
                        mediaDB.deleteMedia(*mediaToDelete.toTypedArray())
                    }
                }
            }

            if (dirPathsToRemove.isNotEmpty()) {
                val dirsToRemove = dirs.filter { dirPathsToRemove.contains(it.path) }
                dirsToRemove.forEach {
                    directoryDB.deleteDirPath(it.path)
                }
                dirs.removeAll(dirsToRemove)
                setupAdapter(dirs)
            }
        } catch (ignored: Exception) {
        }

        val foldersToScan = mLastMediaFetcher!!.getFoldersToScan()
        foldersToScan.remove(FAVORITES)
        foldersToScan.add(0, FAVORITES)
        if (config.showRecycleBinAtFolders) {
            if (foldersToScan.contains(RECYCLE_BIN)) {
                foldersToScan.remove(RECYCLE_BIN)
                foldersToScan.add(0, RECYCLE_BIN)
            } else {
                foldersToScan.add(0, RECYCLE_BIN)
            }
        } else {
            foldersToScan.remove(RECYCLE_BIN)
        }

        dirs.filterNot { it.path == RECYCLE_BIN || it.path == FAVORITES }.forEach {
            foldersToScan.remove(it.path)
        }

        // check the remaining folders which were not cached at all yet
        for (folder in foldersToScan) {
            if (mShouldStopFetching || isDestroyed || isFinishing) {
                return
            }

            val sorting = config.getFolderSorting(folder)
            val grouping = config.getFolderGrouping(folder)
            val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0
                    || sorting and SORT_BY_DATE_TAKEN != 0
                    || grouping and GROUP_BY_DATE_TAKEN_DAILY != 0
                    || grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

            val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0
                    || sorting and SORT_BY_DATE_MODIFIED != 0
                    || grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0
                    || grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

            val newMedia = mLastMediaFetcher!!.getFilesFrom(
                curPath = folder,
                isPickImage = getImagesOnly,
                isPickVideo = getVideosOnly,
                getProperDateTaken = getProperDateTaken,
                getProperLastModified = getProperLastModified,
                getProperFileSize = getProperFileSize,
                favoritePaths = favoritePaths,
                getVideoDurations = false,
                lastModifieds = lastModifieds,
                dateTakens = dateTakens,
                android11Files = android11Files
            )

            if (newMedia.isEmpty()) {
                continue
            }

            if (isPlaceholderVisible) {
                isPlaceholderVisible = false
                runOnUiThread {
                    binding.directoriesEmptyPlaceholder.beGone()
                    binding.directoriesEmptyPlaceholder2.beGone()
                    binding.directoriesFastscroller.beVisible()
                }
            }

            val newDir = createDirectoryFromMedia(
                path = folder,
                curMedia = newMedia,
                albumCovers = albumCovers,
                hiddenString = hiddenString,
                includedFolders = includedFolders,
                getProperFileSize = getProperFileSize,
                noMediaFolders = noMediaFolders
            )
            dirs.add(newDir)
            setupAdapter(dirs)

            // make sure to create a new thread for these operations, dont just use the common bg thread
            Thread {
                try {
                    directoryDB.insert(newDir)
                    if (folder != RECYCLE_BIN && folder != FAVORITES) {
                        mediaDB.insertAll(newMedia)
                    }
                } catch (ignored: Exception) {
                }
            }.start()
        }

        mLoadedInitialPhotos = true
        if (config.appRunCount > 1) {
            checkLastMediaChanged()
        }

        runOnUiThread {
            binding.directoriesRefreshLayout.isRefreshing = false
            checkPlaceholderVisibility(dirs)
        }

        checkInvalidDirectories(dirs)
        if (mDirs.size > 50) {
            excludeSpamFolders()
        }

        val excludedFolders = config.excludedFolders
        val everShownFolders = config.everShownFolders.toMutableSet() as HashSet<String>

        // do not add excluded folders and their subfolders at everShownFolders
        dirs.filter { dir ->
            return@filter !excludedFolders.any { dir.path.startsWith(it) }
        }.mapTo(everShownFolders) { it.path }

        try {
            // scan the internal storage from time to time for new folders
            if (config.appRunCount == 1 || config.appRunCount % 30 == 0) {
                everShownFolders.addAll(getFoldersWithMedia(config.internalStoragePath))
            }

            // catch some extreme exceptions like too many everShownFolders for storing, shouldnt really happen
            config.everShownFolders = everShownFolders
        } catch (e: Exception) {
            config.everShownFolders = HashSet()
        }

        mDirs = dirs.clone() as ArrayList<Directory>
    }

    private fun setAsDefaultFolder() {
        config.defaultFolder = ""
        refreshMenuItems()
    }

    private fun openDefaultFolder() {
        if (config.defaultFolder.isEmpty()) {
            return
        }

        val defaultDir = File(config.defaultFolder)

        if ((!defaultDir.exists() || !defaultDir.isDirectory) && (config.defaultFolder != RECYCLE_BIN && config.defaultFolder != FAVORITES)) {
            config.defaultFolder = ""
            return
        }

        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, config.defaultFolder)
            handleMediaIntent(this)
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Directory>) {
        binding.directoriesEmptyPlaceholder.beVisibleIf(dirs.isEmpty() && mLoadedInitialPhotos)
        binding.directoriesEmptyPlaceholder2.beVisibleIf(dirs.isEmpty() && mLoadedInitialPhotos)

        if (binding.mainMenu.isSearchOpen) {
            binding.directoriesEmptyPlaceholder.text =
                getString(org.fossify.commons.R.string.no_items_found)
            binding.directoriesEmptyPlaceholder2.beGone()
        } else if (dirs.isEmpty() && config.filterMedia == getDefaultFileFilter()) {
            if (isRPlus() && !isExternalStorageManager()) {
                binding.directoriesEmptyPlaceholder.text =
                    getString(org.fossify.commons.R.string.no_items_found)
                binding.directoriesEmptyPlaceholder2.beGone()
            } else {
                binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_add_included)
                binding.directoriesEmptyPlaceholder2.text = getString(R.string.add_folder)
            }

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showAddIncludedFolderDialog {
                    refreshItems()
                }
            }
        } else {
            binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_with_filters)
            binding.directoriesEmptyPlaceholder2.text =
                getString(R.string.change_filters_underlined)

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showFilterMediaDialog()
            }
        }

        binding.directoriesEmptyPlaceholder2.underlineText()
        binding.directoriesFastscroller.beVisibleIf(binding.directoriesEmptyPlaceholder.isGone())
    }

    private fun setupAdapter(
        dirs: ArrayList<Directory>,
        textToSearch: String = binding.mainMenu.getCurrentQuery(),
        forceRecreate: Boolean = false
    ) {
        val currAdapter = binding.directoriesGrid.adapter
        val distinctDirs = dirs
            .distinctBy { it.path.getDistinctPath() }
            .toMutableList() as ArrayList<Directory>

        val sortedDirs = getSortedDirectories(distinctDirs)
        var dirsToShow = getDirsToShow(
            dirs = sortedDirs,
            allDirs = mDirs,
            currentPathPrefix = mCurrentPathPrefix
        ).clone() as ArrayList<Directory>

        if (currAdapter == null || forceRecreate) {
            mDirsIgnoringSearch = dirs
            initZoomListener()
            DirectoryAdapter(
                this,
                dirsToShow,
                this,
                binding.directoriesGrid,
                isPickIntent(intent) || isGetAnyContentIntent(intent),
                binding.directoriesRefreshLayout
            ) {
                val clickedDir = it as Directory
                val path = clickedDir.path
                if (clickedDir.subfoldersCount == 1 || !config.groupDirectSubfolders) {
                    if (path != config.tempFolderPath) {
                        itemClicked(path)
                    }
                } else {
                    mCurrentPathPrefix = path
                    mOpenedSubfolders.add(path)
                    setupAdapter(mDirs, "")
                }
            }.apply {
                setupZoomListener(mZoomListener)
                runOnUiThread {
                    binding.directoriesGrid.adapter = this
                    setupScrollDirection()

                    if (config.viewTypeFolders == VIEW_TYPE_LIST && areSystemAnimationsEnabled) {
                        binding.directoriesGrid.scheduleLayoutAnimation()
                    }
                }
            }
        } else {
            runOnUiThread {
                if (textToSearch.isNotEmpty()) {
                    dirsToShow = dirsToShow
                        .filter { it.name.contains(textToSearch, true) }
                        .sortedBy { !it.name.startsWith(textToSearch, true) }
                        .toMutableList() as ArrayList
                }
                checkPlaceholderVisibility(dirsToShow)

                (binding.directoriesGrid.adapter as? DirectoryAdapter)?.updateDirs(dirsToShow)
            }
        }

        // recyclerview sometimes becomes empty at init/update, triggering an invisible refresh like this seems to work fine
        binding.directoriesGrid.postDelayed({
            binding.directoriesGrid.scrollBy(0, 0)
        }, 500)
    }

    private fun setupScrollDirection() {
        val scrollHorizontally =
            config.scrollHorizontally && config.viewTypeFolders == VIEW_TYPE_GRID
        binding.directoriesFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun checkInvalidDirectories(dirs: ArrayList<Directory>) {
        val invalidDirs = ArrayList<Directory>()
        val OTGPath = config.OTGPath
        dirs.filter { !it.areFavorites() && !it.isRecycleBin() }.forEach {
            if (!getDoesFilePathExist(it.path, OTGPath)) {
                invalidDirs.add(it)
            } else if (it.path != config.tempFolderPath && (!isRPlus() || isExternalStorageManager())) {
                // avoid calling file.list() or listfiles() on Android 11+, it became way too slow
                val children = if (isPathOnOTG(it.path)) {
                    getOTGFolderChildrenNames(it.path)
                } else {
                    File(it.path).list()?.asList()
                }

                val hasMediaFile = children?.any {
                    it != null && (
                            it.isMediaFile()
                                    || (it.startsWith("img_", true)
                                    && File(it).isDirectory)
                            )
                } == true

                if (!hasMediaFile) {
                    invalidDirs.add(it)
                }
            }
        }

        if (getFavoritePaths().isEmpty()) {
            val favoritesFolder = dirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null) {
                invalidDirs.add(favoritesFolder)
            }
        }

        if (config.useRecycleBin) {
            try {
                val binFolder = dirs.firstOrNull { it.path == RECYCLE_BIN }
                if (binFolder != null && mediaDB.getDeletedMedia().isEmpty()) {
                    invalidDirs.add(binFolder)
                }
            } catch (ignored: Exception) {
            }
        }

        if (invalidDirs.isNotEmpty()) {
            dirs.removeAll(invalidDirs)
            setupAdapter(dirs)
            invalidDirs.forEach {
                try {
                    directoryDB.deleteDirPath(it.path)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun getCurrentlyDisplayedDirs() = getRecyclerAdapter()?.dirs ?: ArrayList()

    private fun setupLatestMediaId() {
        ensureBackgroundThread {
            if (hasPermission(PERMISSION_READ_STORAGE)) {
                mLatestMediaId = getLatestMediaId()
                mLatestMediaDateId = getLatestMediaByDateId()
            }
        }
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed) {
            return
        }

        mLastMediaHandler.postDelayed({
            ensureBackgroundThread {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mLatestMediaId != mediaId || mLatestMediaDateId != mediaDateId) {
                    mLatestMediaId = mediaId
                    mLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getDirectories()
                    }
                } else {
                    mLastMediaHandler.removeCallbacksAndMessages(null)
                    checkLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }

    private fun checkRecycleBinItems() {
        if (config.useRecycleBin && config.lastBinCheck < System.currentTimeMillis() - DAY_SECONDS * 1000) {
            config.lastBinCheck = System.currentTimeMillis()
            Handler().postDelayed({
                ensureBackgroundThread {
                    try {
                        val filesToDelete = mediaDB.getOldRecycleBinItems(
                            System.currentTimeMillis() - MONTH_MILLISECONDS
                        )
                        filesToDelete.forEach {
                            if (File(it.path.replaceFirst(RECYCLE_BIN, recycleBinPath)).delete()) {
                                mediaDB.deleteMediumPath(it.path)
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }, 3000L)
        }
    }

    // exclude probably unwanted folders, for example facebook stickers are split between hundreds of separate folders like
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/175139712676531/209575122566323
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/497837993632037/499671223448714
    private fun excludeSpamFolders() {
        ensureBackgroundThread {
            try {
                val internalPath = internalStoragePath
                val checkedPaths = ArrayList<String>()
                val oftenRepeatedPaths = ArrayList<String>()
                val paths = mDirs
                    .map { it.path.removePrefix(internalPath) }
                    .toMutableList() as ArrayList<String>
                paths.forEach {
                    val parts = it.split("/")
                    var currentString = ""
                    for (i in 0 until parts.size) {
                        currentString += "${parts[i]}/"

                        if (!checkedPaths.contains(currentString)) {
                            val cnt = paths.count { it.startsWith(currentString) }
                            if (cnt > 50 && currentString.startsWith("/Android/data", true)) {
                                oftenRepeatedPaths.add(currentString)
                            }
                        }

                        checkedPaths.add(currentString)
                    }
                }

                val substringToRemove = oftenRepeatedPaths.filter {
                    val path = it
                    it == "/" || oftenRepeatedPaths.any { it != path && it.startsWith(path) }
                }

                oftenRepeatedPaths.removeAll(substringToRemove)
                val OTGPath = config.OTGPath
                oftenRepeatedPaths.forEach {
                    val file = File("$internalPath/$it")
                    if (getDoesFilePathExist(file.absolutePath, OTGPath)) {
                        config.addExcludedFolder(file.absolutePath)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun getFoldersWithMedia(path: String): HashSet<String> {
        val folders = HashSet<String>()
        try {
            val files = File(path).listFiles()
            if (files != null) {
                files.sortBy { !it.isDirectory }
                for (file in files) {
                    if (file.isDirectory && !file.startsWith("${config.internalStoragePath}/Android")) {
                        folders.addAll(getFoldersWithMedia(file.absolutePath))
                    } else if (file.isFile && file.isMediaFile()) {
                        folders.add(file.parent ?: "")
                        break
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return folders
    }

    override fun refreshItems() {
        if (mShowingPhotos) {
            getPhotos()
        } else {
            getDirectories()
        }
    }

    override fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean) {
        val filtered = fileDirItems
            .filter { !getIsPathDirectory(it.path) && it.path.isMediaFile() } as ArrayList
        if (filtered.isEmpty()) {
            return
        }

        if (
            config.useRecycleBin
            && !skipRecycleBin
            && !filtered.first().path.startsWith(recycleBinPath)
        ) {
            val movingItems = resources.getQuantityString(
                org.fossify.commons.R.plurals.moving_items_into_bin,
                filtered.size,
                filtered.size
            )
            toast(movingItems)

            movePathsInRecycleBin(filtered.map { it.path } as ArrayList<String>) {
                if (it) {
                    deleteFilteredFiles(filtered)
                } else {
                    toast(org.fossify.commons.R.string.unknown_error_occurred)
                }
            }
        } else {
            val deletingItems = resources.getQuantityString(
                org.fossify.commons.R.plurals.deleting_items,
                filtered.size,
                filtered.size
            )
            toast(deletingItems)
            deleteFilteredFiles(filtered)
        }
    }

    private fun deleteFilteredFiles(filtered: ArrayList<FileDirItem>) {
        deleteFiles(filtered) {
            if (!it) {
                toast(org.fossify.commons.R.string.unknown_error_occurred)
                return@deleteFiles
            }

            mPhotos.removeAll { item -> filtered.map { it.path }.contains((item as? Medium)?.path) }

            ensureBackgroundThread {
                val useRecycleBin = config.useRecycleBin
                filtered.forEach {
                    if (it.path.startsWith(recycleBinPath) || !useRecycleBin) {
                        deleteDBPath(it.path)
                    }
                }
            }
        }
    }

    // picking multiple items from the photos is handled by MediaActivity
    override fun selectedPaths(paths: ArrayList<String>) = Unit

    override fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>) {
        var currentGridPosition = 0
        media.forEach {
            if (it is Medium) {
                it.gridPosition = currentGridPosition++
            } else if (it is ThumbnailSection) {
                currentGridPosition = 0
            }
        }

        if (binding.photosGrid.itemDecorationCount > 0) {
            val currentGridDecoration =
                binding.photosGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
            currentGridDecoration.items = media
        }
    }

    override fun recheckPinnedFolders() {
        ensureBackgroundThread {
            gotDirectories(movePinnedDirectoriesToFront(getCurrentlyDisplayedDirs()))
        }
    }

    override fun updateDirectories(directories: ArrayList<Directory>) {
        ensureBackgroundThread {
            storeDirectoryItems(directories)
            removeInvalidDBDirectories()
        }
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
