import sys
import json
import numpy as np
import pandas as pd
from pathlib import Path
from datetime import datetime, timedelta

def load_location_data():
    """Load location data from CSV"""
    try:
        locations_df = pd.read_csv('locationData.csv')
        return locations_df['location_id'].unique().tolist()
    except Exception as e:
        print(f"Error loading location data: {str(e)}", file=sys.stderr)
        return []

def generate_sample_weather_data(location_id):
    """Generate sample weather data for testing with location specifics"""
    sample_data = []
    current_date = datetime.now()

    # Add some variation based on location_id to simulate different locations
    temp_base = 25 + (location_id % 5)  # Different base temperature per location
    wind_base = 5 + (location_id % 3)   # Different base wind speed per location

    for i in range(7):  # Generate 7 days of data
        date = current_date + timedelta(days=i)
        sample_data.append({
            'location_id': location_id,
            'date': date.strftime('%Y-%m-%d'),
            'temperature_2m_max': round(np.random.uniform(temp_base, temp_base + 10), 2),
            'temperature_2m_min': round(np.random.uniform(temp_base - 5, temp_base), 2),
            'precipitation_sum': round(np.random.uniform(0, 50), 2),
            'wind_speed_10m_max': round(np.random.uniform(wind_base, wind_base + 10), 2),
            'wind_gusts_10m_max': round(np.random.uniform(wind_base + 5, wind_base + 15), 2),
            'shortwave_radiation_sum': round(np.random.uniform(10, 20), 2)
        })
    return sample_data

def process_weather_data(data):
    """Process weather data to create predictions"""
    results = []
    for day in data:
        # Calculate predictions based on data
        temp = (day['temperature_2m_max'] + day['temperature_2m_min']) / 2
        rain_prob = min(day['precipitation_sum'] / 50.0, 1.0)

        # Determine weather type
        if rain_prob > 0.7:
            weather_type = 'HEAVY_RAIN'
        elif rain_prob > 0.4:
            weather_type = 'MODERATE_RAIN'
        elif rain_prob > 0.2:
            weather_type = 'LIGHT_RAIN'
        elif temp > 30:
            weather_type = 'SUNNY'
        else:
            weather_type = 'FAIR'

        results.append({
            'location_id': day['location_id'],
            'date': day['date'],
            'temperature': round(temp, 1),
            'rainfall_probability': round(rain_prob, 2),
            'wind_speed': round(day['wind_speed_10m_max'], 1),
            'weather_type': weather_type
        })
    return results

def predict_weather():
    try:
        location_ids = load_location_data()
        if not location_ids:
            print("No location IDs found, using default locations")
            location_ids = [1, 2, 3, 4, 5]  # Default locations if CSV fails

        all_predictions = []
        for location_id in location_ids:
            # Generate sample data for each location
            input_data = generate_sample_weather_data(location_id)
            predictions = process_weather_data(input_data)
            all_predictions.extend(predictions)

        print(json.dumps(all_predictions, indent=2))
        return all_predictions

    except Exception as e:
        print(f"Error making prediction: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    predict_weather()