package fr.free.nrw.commons.repository;

import androidx.annotation.Nullable;
import fr.free.nrw.commons.category.CategoriesModel;
import fr.free.nrw.commons.category.CategoryItem;
import fr.free.nrw.commons.contributions.Contribution;
import fr.free.nrw.commons.contributions.ContributionDao;
import fr.free.nrw.commons.filepicker.UploadableFile;
import fr.free.nrw.commons.location.LatLng;
import fr.free.nrw.commons.nearby.NearbyPlaces;
import fr.free.nrw.commons.nearby.Place;
import fr.free.nrw.commons.upload.ImageCoordinates;
import fr.free.nrw.commons.upload.SimilarImageInterface;
import fr.free.nrw.commons.upload.UploadController;
import fr.free.nrw.commons.upload.UploadItem;
import fr.free.nrw.commons.upload.UploadModel;
import fr.free.nrw.commons.upload.structure.depictions.DepictModel;
import fr.free.nrw.commons.upload.structure.depictions.DepictedItem;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import timber.log.Timber;

/**
 * The repository class for UploadActivity
 */
@Singleton
public class UploadRepository {

    private final UploadModel uploadModel;
    private final UploadController uploadController;
    private final CategoriesModel categoriesModel;
    private final NearbyPlaces nearbyPlaces;
    private final DepictModel depictModel;

    private static final double NEARBY_RADIUS_IN_KILO_METERS = 0.1; //100 meters
    private final ContributionDao contributionDao;

    @Inject
    public UploadRepository(UploadModel uploadModel,
        UploadController uploadController,
        CategoriesModel categoriesModel,
        NearbyPlaces nearbyPlaces,
        DepictModel depictModel,
        ContributionDao contributionDao) {
        this.uploadModel = uploadModel;
        this.uploadController = uploadController;
        this.categoriesModel = categoriesModel;
        this.nearbyPlaces = nearbyPlaces;
        this.depictModel = depictModel;
        this.contributionDao=contributionDao;
    }

    /**
     * asks the RemoteDataSource to build contributions
     *
     * @return
     */
    public Observable<Contribution> buildContributions() {
        return uploadModel.buildContributions();
    }

    /**
     * asks the RemoteDataSource to start upload for the contribution
     *
     * @param contribution
     */

    public void prepareMedia(Contribution contribution) {
        uploadController.prepareMedia(contribution);
    }


    public void saveContribution(Contribution contribution) {
        contributionDao.save(contribution).blockingAwait();
    }

    /**
     * Fetches and returns all the Upload Items
     *
     * @return
     */
    public List<UploadItem> getUploads() {
        return uploadModel.getUploads();
    }

    /**
     *Prepare for a fresh upload
     */
    public void cleanup() {
        uploadModel.cleanUp();
        //This needs further refactoring, this should not be here, right now the structure wont suppoort rhis
        categoriesModel.cleanUp();
        depictModel.cleanUp();
    }

    /**
     * Fetches and returns the selected categories for the current upload
     *
     * @return
     */
    public List<CategoryItem> getSelectedCategories() {
        return categoriesModel.getSelectedCategories();
    }

    /**
     * all categories from MWApi
     *
     * @param query
     * @param imageTitleList
     * @param selectedDepictions
     * @return
     */
    public Observable<List<CategoryItem>> searchAll(String query, List<String> imageTitleList,
        List<DepictedItem> selectedDepictions) {
        return categoriesModel.searchAll(query, imageTitleList, selectedDepictions);
    }

    /**
     * sets the list of selected categories for the current upload
     *
     * @param categoryStringList
     */
    public void setSelectedCategories(List<String> categoryStringList) {
        uploadModel.setSelectedCategories(categoryStringList);
    }

    /**
     * handles the category selection/deselection
     *
     * @param categoryItem
     */
    public void onCategoryClicked(CategoryItem categoryItem) {
        categoriesModel.onCategoryItemClicked(categoryItem);
    }

    /**
     * prunes the category list for irrelevant categories see #750
     *
     * @param name
     * @return
     */
    public boolean containsYear(String name) {
        return categoriesModel.containsYear(name);
    }

    /**
     * retursn the string list of available license from the LocalDataSource
     *
     * @return
     */
    public List<String> getLicenses() {
        return uploadModel.getLicenses();
    }

    /**
     * returns the selected license for the current upload
     *
     * @return
     */
    public String getSelectedLicense() {
        return uploadModel.getSelectedLicense();
    }

    /**
     * returns the number of Upload Items
     *
     * @return
     */
    public int getCount() {
        return uploadModel.getCount();
    }

    /**
     * ask the RemoteDataSource to pre process the image
     *
     * @param uploadableFile
     * @param place
     * @param similarImageInterface
     * @return
     */
    public Observable<UploadItem> preProcessImage(UploadableFile uploadableFile, Place place,
        SimilarImageInterface similarImageInterface) {
        return uploadModel.preProcessImage(uploadableFile, place,
            similarImageInterface);
    }

    /**
     * query the RemoteDataSource for image quality
     *
     * @param uploadItem
     * @return
     */
    public Single<Integer> getImageQuality(UploadItem uploadItem) {
        return uploadModel.getImageQuality(uploadItem);
    }

    /**
     * asks the LocalDataSource to delete the file with the given file path
     *
     * @param filePath
     */
    public void deletePicture(String filePath) {
        uploadModel.deletePicture(filePath);
    }

    /**
     * fetches and returns the upload item
     *
     * @param index
     * @return
     */
    public UploadItem getUploadItem(int index) {
        if (index >= 0) {
            return uploadModel.getItems().get(index);
        }
        return null; //There is no item to copy details
    }

    /**
     * set selected license for the current upload
     *
     * @param licenseName
     */
    public void setSelectedLicense(String licenseName) {
        uploadModel.setSelectedLicense(licenseName);
    }

    public void onDepictItemClicked(DepictedItem depictedItem) {
        uploadModel.onDepictItemClicked(depictedItem);
    }

    /**
     * Fetches and returns the selected depictions for the current upload
     *
     * @return
     */

    public List<DepictedItem> getSelectedDepictions() {
        return uploadModel.getSelectedDepictions();
    }

    /**
     * Search all depictions from
     *
     * @param query
     * @return
     */

    public Flowable<List<DepictedItem>> searchAllEntities(String query) {
        return depictModel.searchAllEntities(query, this);
    }

    /**
     * Gets the depiction for each unique {@link Place} associated with an {@link UploadItem}
     * from {@link #getUploads()}
     *
     * @return a single that provides the depictions
     */
    public Single<List<DepictedItem>> getPlaceDepictions() {
        final Set<String> qids = new HashSet<>();
        for (final UploadItem item : getUploads()) {
            final Place place = item.getPlace();
            if (place != null) {
                qids.add(place.getWikiDataEntityId());
            }
        }
        return depictModel.getPlaceDepictions(new ArrayList<>(qids));
    }

    /**
     * Returns nearest place matching the passed latitude and longitude
     * @param decLatitude
     * @param decLongitude
     * @return
     */
    @Nullable
    public Place checkNearbyPlaces(final double decLatitude, final double decLongitude) {
        try {
            final List<Place> fromWikidataQuery = nearbyPlaces.getFromWikidataQuery(new LatLng(
                    decLatitude, decLongitude, 0.0f),
                    Locale.getDefault().getLanguage(),
                    NEARBY_RADIUS_IN_KILO_METERS, false, null);
            return (fromWikidataQuery != null && fromWikidataQuery.size() > 0) ? fromWikidataQuery
                .get(0) : null;
        }catch (final Exception e) {
            Timber.e("Error fetching nearby places: %s", e.getMessage());
            return null;
        }
    }

    public void useSimilarPictureCoordinates(ImageCoordinates imageCoordinates, int uploadItemIndex) {
        uploadModel.useSimilarPictureCoordinates(imageCoordinates, uploadItemIndex);
    }

    public boolean isWMLSupportedForThisPlace() {
        return uploadModel.getItems().get(0).isWLMUpload();
    }
}
