package com.reloader.biometricface.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.common.RequestMethod;
import com.microsoft.projectoxford.face.contract.AddPersistedFaceResult;
import com.microsoft.projectoxford.face.contract.CreatePersonResult;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceList;
import com.microsoft.projectoxford.face.contract.FaceListMetadata;
import com.microsoft.projectoxford.face.contract.FaceMetadata;
import com.microsoft.projectoxford.face.contract.FaceRectangle;
import com.microsoft.projectoxford.face.contract.GroupResult;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.LargeFaceList;
import com.microsoft.projectoxford.face.contract.LargePersonGroup;
import com.microsoft.projectoxford.face.contract.Person;
import com.microsoft.projectoxford.face.contract.PersonFace;
import com.microsoft.projectoxford.face.contract.PersonGroup;
import com.microsoft.projectoxford.face.contract.SimilarFace;
import com.microsoft.projectoxford.face.contract.SimilarPersistedFace;
import com.microsoft.projectoxford.face.contract.TrainingStatus;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.microsoft.projectoxford.face.rest.ClientException;
import com.microsoft.projectoxford.face.rest.WebServiceRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FaceServiceResApiClient implements FaceServiceClient {

    private final WebServiceRequest mRestCall;
    private Gson mGson;
    private static final String DEFAULT_API_ROOT = "https://westus.api.cognitive.microsoft.com/face/v1.0";
    private final String mServiceHost;
    private static final String DETECT_QUERY = "detect";
    private static final String VERIFY_QUERY = "verify";
    private static final String TRAIN_QUERY = "train";
    private static final String TRAINING_QUERY = "training";
    private static final String IDENTIFY_QUERY = "identify";
    private static final String PERSON_GROUPS_QUERY = "persongroups";
    private static final String LARGE_PERSON_GROUPS_QUERY = "largepersongroups";
    private static final String PERSONS_QUERY = "persons";
    private static final String FACE_LISTS_QUERY = "facelists";
    private static final String LARGE_FACE_LISTS_QUERY = "largefacelists";
    private static final String PERSISTED_FACES_QUERY = "persistedfaces";
    private static final String GROUP_QUERY = "group";
    private static final String FIND_SIMILARS_QUERY = "findsimilars";
    private static final String STREAM_DATA = "application/octet-stream";
    private static final String DATA = "data";

    public FaceServiceResApiClient(String subscriptionKey) {
        this("https://westus.api.cognitive.microsoft.com/face/v1.0", subscriptionKey);
    }

    public FaceServiceResApiClient(String serviceHost, String subscriptionKey) {
        this.mGson = (new GsonBuilder()).setDateFormat("MM/dd/yyyy HH:mm:ss").create();
        this.mServiceHost = serviceHost.replaceAll("/$", "");
        this.mRestCall = new WebServiceRequest(subscriptionKey);
    }

    public Face[] detect(String url, boolean returnFaceId, boolean returnFaceLandmarks, FaceAttributeType[] returnFaceAttributes) throws ClientException, IOException {
        Map<String, Object> params = new HashMap();
        params.put("returnFaceId", returnFaceId);
        params.put("returnFaceLandmarks", returnFaceLandmarks);
        if (returnFaceAttributes != null && returnFaceAttributes.length > 0) {
            StringBuilder faceAttributesStringBuilder = new StringBuilder();
            boolean firstAttribute = true;
            FaceAttributeType[] var8 = returnFaceAttributes;
            int var9 = returnFaceAttributes.length;

            for (int var10 = 0; var10 < var9; ++var10) {
                FaceAttributeType faceAttributeType = var8[var10];
                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    faceAttributesStringBuilder.append(",");
                }

                faceAttributesStringBuilder.append(faceAttributeType);
            }

            params.put("returnFaceAttributes", faceAttributesStringBuilder.toString());
        }

        String path = String.format("%s/%s", this.mServiceHost, "detect");
        String uri = WebServiceRequest.getUrl(path, params);
        params.clear();
        params.put("url", url);
        String json = (String) this.mRestCall.request(uri, RequestMethod.POST, params, (String) null);
        Type listType = (new TypeToken<List<Face>>() {
        }).getType();
        List<Face> faces = (List) this.mGson.fromJson(json, listType);
        return (Face[]) faces.toArray(new Face[faces.size()]);
    }

    public Face[] detect(InputStream imageStream, boolean returnFaceId, boolean returnFaceLandmarks, FaceAttributeType[] returnFaceAttributes) throws ClientException, IOException {
        Map<String, Object> params = new HashMap();
        params.put("returnFaceId", returnFaceId);
        params.put("returnFaceLandmarks", returnFaceLandmarks);
        int bytesRead;
        if (returnFaceAttributes != null && returnFaceAttributes.length > 0) {
            StringBuilder faceAttributesStringBuilder = new StringBuilder();
            boolean firstAttribute = true;
            FaceAttributeType[] var8 = returnFaceAttributes;
            bytesRead = returnFaceAttributes.length;

            for (int var10 = 0; var10 < bytesRead; ++var10) {
                FaceAttributeType faceAttributeType = var8[var10];
                if (firstAttribute) {
                    firstAttribute = false;
                } else {
                    faceAttributesStringBuilder.append(",");
                }

                faceAttributesStringBuilder.append(faceAttributeType);
            }

            params.put("returnFaceAttributes", faceAttributesStringBuilder.toString());
        }

        String path = String.format("%s/%s", this.mServiceHost, "detect");
        String uri = WebServiceRequest.getUrl(path, params);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];

        while ((bytesRead = imageStream.read(bytes)) > 0) {
            byteArrayOutputStream.write(bytes, 0, bytesRead);
        }

        byte[] data = byteArrayOutputStream.toByteArray();
        params.clear();
        params.put("data", data);
        String json = (String) this.mRestCall.request(uri, RequestMethod.POST, params, "application/octet-stream");
        Type listType = (new TypeToken<List<Face>>() {
        }).getType();
        List<Face> faces = (List) this.mGson.fromJson(json, listType);
        return (Face[]) faces.toArray(new Face[faces.size()]);
    }

    public VerifyResult verify(UUID faceId1, UUID faceId2) throws ClientException, IOException {
        Map<String, Object> params = new HashMap();
        String uri = String.format("%s/%s", this.mServiceHost, "verify");
        params.put("faceId1", faceId1);
        params.put("faceId2", faceId2);
        String json = (String) this.mRestCall.request(uri, RequestMethod.POST, params, (String) null);
        return (VerifyResult) this.mGson.fromJson(json, VerifyResult.class);
    }

    @Override
    public VerifyResult verify(UUID uuid, String s, UUID uuid1) throws ClientException, IOException {
        return null;
    }

    @Override
    public VerifyResult verifyInPersonGroup(UUID uuid, String s, UUID uuid1) throws ClientException, IOException {
        return null;
    }

    @Override
    public VerifyResult verifyInLargePersonGroup(UUID uuid, String s, UUID uuid1) throws ClientException, IOException {
        return null;
    }

    @Override
    public IdentifyResult[] identity(String s, UUID[] uuids, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }

    @Override
    public IdentifyResult[] identity(String s, UUID[] uuids, float v, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }


    @Override
    public IdentifyResult[] identityInPersonGroup(String s, UUID[] uuids, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }

    @Override
    public IdentifyResult[] identityInPersonGroup(String s, UUID[] uuids, float v, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }

    @Override
    public IdentifyResult[] identityInLargePersonGroup(String s, UUID[] uuids, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }

    @Override
    public IdentifyResult[] identityInLargePersonGroup(String s, UUID[] uuids, float v, int i) throws ClientException, IOException {
        return new IdentifyResult[0];
    }

    @Override
    public SimilarFace[] findSimilar(UUID uuid, UUID[] uuids, int i) throws ClientException, IOException {
        return new SimilarFace[0];
    }

    @Override
    public SimilarFace[] findSimilar(UUID uuid, UUID[] uuids, int i, FindSimilarMatchMode findSimilarMatchMode) throws ClientException, IOException {
        return new SimilarFace[0];
    }

    public SimilarPersistedFace[] findSimilar(UUID faceId, String faceListId, int maxNumOfCandidatesReturned) throws ClientException, IOException {
        return this.findSimilar(faceId, faceListId, maxNumOfCandidatesReturned, FindSimilarMatchMode.matchPerson);
    }

    public SimilarPersistedFace[] findSimilar(UUID faceId, String faceListId, int maxNumOfCandidatesReturned, FindSimilarMatchMode mode) throws ClientException, IOException {
        Map<String, Object> params = new HashMap();
        String uri = String.format("%s/%s", this.mServiceHost, "findsimilars");
        params.put("faceId", faceId);
        params.put("faceListId", faceListId);
        params.put("maxNumOfCandidatesReturned", maxNumOfCandidatesReturned);
        params.put("mode", mode.toString());
        String json = (String) this.mRestCall.request(uri, RequestMethod.POST, params, (String) null);
        Type listType = (new TypeToken<List<SimilarPersistedFace>>() {
        }).getType();
        List<SimilarPersistedFace> result = (List) this.mGson.fromJson(json, listType);
        return (SimilarPersistedFace[]) result.toArray(new SimilarPersistedFace[result.size()]);
    }

    public SimilarPersistedFace[] findSimilarInFaceList(UUID faceId, String faceListId, int maxNumOfCandidatesReturned) throws ClientException, IOException {
        return this.findSimilar(faceId, faceListId, maxNumOfCandidatesReturned, FindSimilarMatchMode.matchPerson);
    }

    public SimilarPersistedFace[] findSimilarInFaceList(UUID faceId, String faceListId, int maxNumOfCandidatesReturned, FindSimilarMatchMode mode) throws ClientException, IOException {
        return this.findSimilar(faceId, faceListId, maxNumOfCandidatesReturned, mode);
    }

    @Override
    public SimilarPersistedFace[] findSimilarInLargeFaceList(UUID uuid, String s, int i) throws ClientException, IOException {
        return new SimilarPersistedFace[0];
    }

    @Override
    public SimilarPersistedFace[] findSimilarInLargeFaceList(UUID uuid, String s, int i, FindSimilarMatchMode findSimilarMatchMode) throws ClientException, IOException {
        return new SimilarPersistedFace[0];
    }

    @Override
    public GroupResult group(UUID[] uuids) throws ClientException, IOException {
        return null;
    }

    @Override
    public void createPersonGroup(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public void deletePersonGroup(String s) throws ClientException, IOException {

    }

    @Override
    public void updatePersonGroup(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public PersonGroup getPersonGroup(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public PersonGroup[] getPersonGroups() throws ClientException, IOException {
        return new PersonGroup[0];
    }

    @Override
    public PersonGroup[] listPersonGroups(String s, int i) throws ClientException, IOException {
        return new PersonGroup[0];
    }

    @Override
    public PersonGroup[] listPersonGroups(String s) throws ClientException, IOException {
        return new PersonGroup[0];
    }

    @Override
    public PersonGroup[] listPersonGroups(int i) throws ClientException, IOException {
        return new PersonGroup[0];
    }

    @Override
    public PersonGroup[] listPersonGroups() throws ClientException, IOException {
        return new PersonGroup[0];
    }

    @Override
    public void trainPersonGroup(String s) throws ClientException, IOException {

    }

    @Override
    public TrainingStatus getPersonGroupTrainingStatus(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public void createLargePersonGroup(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public void deleteLargePersonGroup(String s) throws ClientException, IOException {

    }

    @Override
    public void updateLargePersonGroup(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public LargePersonGroup getLargePersonGroup(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public LargePersonGroup[] listLargePersonGroups(String s, int i) throws ClientException, IOException {
        return new LargePersonGroup[0];
    }

    @Override
    public LargePersonGroup[] listLargePersonGroups(String s) throws ClientException, IOException {
        return new LargePersonGroup[0];
    }

    @Override
    public LargePersonGroup[] listLargePersonGroups(int i) throws ClientException, IOException {
        return new LargePersonGroup[0];
    }

    @Override
    public LargePersonGroup[] listLargePersonGroups() throws ClientException, IOException {
        return new LargePersonGroup[0];
    }

    @Override
    public void trainLargePersonGroup(String s) throws ClientException, IOException {

    }

    @Override
    public TrainingStatus getLargePersonGroupTrainingStatus(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public CreatePersonResult createPerson(String s, String s1, String s2) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deletePerson(String s, UUID uuid) throws ClientException, IOException {

    }

    @Override
    public void updatePerson(String s, UUID uuid, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public Person getPerson(String s, UUID uuid) throws ClientException, IOException {
        return null;
    }

    @Override
    public Person[] getPersons(String s) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersons(String s, String s1, int i) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersons(String s, String s1) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersons(String s, int i) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersons(String s) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public AddPersistedFaceResult addPersonFace(String s, UUID uuid, String s1, String s2, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public AddPersistedFaceResult addPersonFace(String s, UUID uuid, InputStream inputStream, String s1, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deletePersonFace(String s, UUID uuid, UUID uuid1) throws ClientException, IOException {

    }

    @Override
    public void updatePersonFace(String s, UUID uuid, UUID uuid1, String s1) throws ClientException, IOException {

    }

    @Override
    public PersonFace getPersonFace(String s, UUID uuid, UUID uuid1) throws ClientException, IOException {
        return null;
    }

    @Override
    public CreatePersonResult createPersonInLargePersonGroup(String s, String s1, String s2) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deletePersonInLargePersonGroup(String s, UUID uuid) throws ClientException, IOException {

    }

    @Override
    public void updatePersonInLargePersonGroup(String s, UUID uuid, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public Person getPersonInLargePersonGroup(String s, UUID uuid) throws ClientException, IOException {
        return null;
    }

    @Override
    public Person[] listPersonsInLargePersonGroup(String s, String s1, int i) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersonsInLargePersonGroup(String s, String s1) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersonsInLargePersonGroup(String s, int i) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public Person[] listPersonsInLargePersonGroup(String s) throws ClientException, IOException {
        return new Person[0];
    }

    @Override
    public AddPersistedFaceResult addPersonFaceInLargePersonGroup(String s, UUID uuid, String s1, String s2, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public AddPersistedFaceResult addPersonFaceInLargePersonGroup(String s, UUID uuid, InputStream inputStream, String s1, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deletePersonFaceInLargePersonGroup(String s, UUID uuid, UUID uuid1) throws ClientException, IOException {

    }

    @Override
    public void updatePersonFaceInLargePersonGroup(String s, UUID uuid, UUID uuid1, String s1) throws ClientException, IOException {

    }

    @Override
    public PersonFace getPersonFaceInLargePersonGroup(String s, UUID uuid, UUID uuid1) throws ClientException, IOException {
        return null;
    }

    @Override
    public void createFaceList(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public void deleteFaceList(String s) throws ClientException, IOException {

    }

    @Override
    public void updateFaceList(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public FaceList getFaceList(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public FaceListMetadata[] listFaceLists() throws ClientException, IOException {
        return new FaceListMetadata[0];
    }

    @Override
    public AddPersistedFaceResult addFacesToFaceList(String s, String s1, String s2, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public AddPersistedFaceResult AddFaceToFaceList(String s, InputStream inputStream, String s1, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deleteFacesFromFaceList(String s, UUID uuid) throws ClientException, IOException {

    }

    @Override
    public void createLargeFaceList(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public void deleteLargeFaceList(String s) throws ClientException, IOException {

    }

    @Override
    public LargeFaceList getLargeFaceList(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public LargeFaceList[] listLargeFaceLists(String s, int i) throws ClientException, IOException {
        return new LargeFaceList[0];
    }

    @Override
    public LargeFaceList[] listLargeFaceLists(String s) throws ClientException, IOException {
        return new LargeFaceList[0];
    }

    @Override
    public LargeFaceList[] listLargeFaceLists(int i) throws ClientException, IOException {
        return new LargeFaceList[0];
    }

    @Override
    public LargeFaceList[] listLargeFaceLists() throws ClientException, IOException {
        return new LargeFaceList[0];
    }

    @Override
    public void updateLargeFaceList(String s, String s1, String s2) throws ClientException, IOException {

    }

    @Override
    public void trainLargeFaceList(String s) throws ClientException, IOException {

    }

    @Override
    public TrainingStatus getLargeFaceListTrainingStatus(String s) throws ClientException, IOException {
        return null;
    }

    @Override
    public AddPersistedFaceResult addFacesToLargeFaceList(String s, String s1, String s2, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public AddPersistedFaceResult AddFaceToLargeFaceList(String s, InputStream inputStream, String s1, FaceRectangle faceRectangle) throws ClientException, IOException {
        return null;
    }

    @Override
    public void deleteFaceFromLargeFaceList(String s, UUID uuid) throws ClientException, IOException {

    }

    @Override
    public void updateFaceFromLargeFaceList(String s, UUID uuid, String s1) throws ClientException, IOException {

    }

    @Override
    public FaceMetadata getFaceFromLargeFaceList(String s, UUID uuid) throws ClientException, IOException {
        return null;
    }

    @Override
    public FaceMetadata[] listFacesFromLargeFaceList(String s, String s1, int i) throws ClientException, IOException {
        return new FaceMetadata[0];
    }

    @Override
    public FaceMetadata[] listFacesFromLargeFaceList(String s, String s1) throws ClientException, IOException {
        return new FaceMetadata[0];
    }

    @Override
    public FaceMetadata[] listFacesFromLargeFaceList(String s, int i) throws ClientException, IOException {
        return new FaceMetadata[0];
    }

    @Override
    public FaceMetadata[] listFacesFromLargeFaceList(String s) throws ClientException, IOException {
        return new FaceMetadata[0];
    }
}

