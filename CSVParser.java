import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.opencsv.CSVWriter;

public class CSVParser {

    public static void main(String[] args) {
        String filePath = "tb_rssi.txt"; // Path to the input data file
        String outputFilePath = "new_tb_rssi.csv"; // Output file path
        try {
            Map<String, Map<Integer, Map<String, String>>> data = readData(filePath);
            writeCSV(data, outputFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Map<Integer, Map<String, String>>> readData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        Map<String, Map<Integer, Map<String, String>>> data = new TreeMap<>();

        // Skip the header line
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            String position = parts[1].trim();
            int timeduration = Integer.parseInt(parts[3].trim()) - 1; // Start timeduration from 0
            String bssid = parts[5].trim();
            String rssi = parts[6].trim();

            Map<Integer, Map<String, String>> positionData = data.getOrDefault(position, new TreeMap<>());
            Map<String, String> timedurationData = positionData.getOrDefault(timeduration, new HashMap<>());
            timedurationData.put(bssid, rssi);

            positionData.put(timeduration, timedurationData);
            data.put(position, positionData);
        }

        reader.close();
        return data;
    }

    public static void writeCSV(Map<String, Map<Integer, Map<String, String>>> data, String filePath) throws IOException {
        CSVWriter writer = new CSVWriter(new FileWriter(filePath));

        // Write headers
        Set<String> uniqueBssids = new HashSet<>();
        for (Map<Integer, Map<String, String>> timedurationData : data.values()) {
            for (Map<String, String> wifiData : timedurationData.values()) {
                uniqueBssids.addAll(wifiData.keySet());
            }
        }

        List<String> headers = new ArrayList<>();
        headers.add("position");
        headers.add("timeduration");
        headers.addAll(uniqueBssids);

        writer.writeNext(headers.toArray(new String[0]));

        // Write data
        for (Map.Entry<String, Map<Integer, Map<String, String>>> entry : data.entrySet()) {
            String position = entry.getKey();
            Map<Integer, Map<String, String>> positionData = entry.getValue();

            for (Map.Entry<Integer, Map<String, String>> timedurationEntry : positionData.entrySet()) {
                int timeduration = timedurationEntry.getKey();
                Map<String, String> wifiData = timedurationEntry.getValue();

                List<String> parts = new ArrayList<>();
                parts.add(position);
                parts.add(String.valueOf(timeduration));

                for (String bssid : uniqueBssids) {
                    parts.add(wifiData.getOrDefault(bssid, "-120"));
                }

                writer.writeNext(parts.toArray(new String[0]));
            }
        }

        writer.close();
    }
}
