package oajava.nycdata;





import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by sbobba on 8/22/2017.
 */
public class RESTHelper {


    private OA_IPHelper oa_iphelper = null;
    public RESTHelper()
    {
        oa_iphelper = new OA_IPHelper();

    }

    ArrayList<String> getResponseFromAPI (String url, long m_tmHandle) throws Exception
    {

        //Send GET Request and read the response
        URL endpoint = new URL(url);
        HttpURLConnection connection = (HttpURLConnection)endpoint.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        ArrayList<String> responseData = new ArrayList<>();
        responseData.add(0, String.valueOf(responseCode));
        responseData.add(1, response.toString());

        return responseData;
    }


    //Sends a GET request to API to fetch metadata, Parses the response and adds it to a HashMap. The HashMap contains
    HashMap<String, Integer> getMetadata (String uniqueResourceID, long m_tmHandle)
    {
        String url = "https://data.cityofnewyork.us/api/views/" +  uniqueResourceID + "/columns.json";
        ArrayList<String> rawMetadataResponse = null;
        HashMap<String, Integer> metadataMap = new HashMap<>();
        try {
             rawMetadataResponse = getResponseFromAPI(url, m_tmHandle);
        }
        catch(MalformedURLException ex)
        {
            return null;
        }
        catch (java.lang.Exception ex)
        {
            return null;
        }

        if(rawMetadataResponse.get(0).equalsIgnoreCase("200")) {
            JSONArray metaJSON = null;
            try {
                 metaJSON = new JSONArray(rawMetadataResponse.get(1));
            }
            catch (Exception ex)
            {
                int j =0;
            }
            int numOfColumns = metaJSON.length();
            for(int i = 0 ; i < numOfColumns; i++)
            {
                JSONObject columnInfo = (JSONObject) metaJSON.get(i);
                metadataMap.put(columnInfo.getString("fieldName"), oa_iphelper.map_REST_to_OA_datatypes(columnInfo.getString("dataTypeName")));
            }
        }
        else
        {
            return null;
        }

        return metadataMap;

    }
}
