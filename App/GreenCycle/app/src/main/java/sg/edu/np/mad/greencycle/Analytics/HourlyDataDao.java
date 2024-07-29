package sg.edu.np.mad.greencycle.Analytics;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HourlyDataDao {
    @Insert
    void insertAll(HourlyData... hourlyData);

    @Query("SELECT * FROM hourlydata WHERE timestamp LIKE :date || '%' AND tankId = :tankId")
    List<HourlyData> getDataForDateAndTank(String date, String tankId);

    @Query("DELETE FROM hourlydata WHERE timestamp LIKE :date || '%' AND tankId = :tankId")
    void deleteDataForDateAndTank(String date, String tankId);

    @Query("SELECT date(timestamp) as day, AVG(ec) as avg_ec, AVG(moisture) as avg_moisture, AVG(nitrogen) as avg_nitrogen, AVG(potassium) as avg_potassium, AVG(phosphorous) as avg_phosphorous, AVG(temperature) as avg_temperature, AVG(humidity) as avg_humidity, AVG(ph) as avg_ph FROM hourlydata WHERE timestamp BETWEEN :startDate AND :endDate AND tankId = :tankId GROUP BY day ORDER BY day")
    List<DailyAggregate> getDailyAggregates(String startDate, String endDate, String tankId);

    @Query("SELECT strftime('%Y-%m', date(timestamp) )as month, AVG(ec) as avg_ec, AVG(moisture) as avg_moisture, AVG(nitrogen) as avg_nitrogen, AVG(potassium) as avg_potassium, AVG(phosphorous) as avg_phosphorous, AVG(temperature) as avg_temperature, AVG(ph) as avg_ph FROM hourlydata WHERE timestamp BETWEEN :startDate AND :endDate AND tankId = :tankId GROUP BY month ORDER BY month")
    List<MonthlyAggregate> getMonthlyAggregates(String startDate, String endDate, String tankId);
    ;



    @Query("DELETE FROM hourlydata")
    void deleteAll();


}
