import sys
import json
import numpy as np
from pathlib import Path
from datetime import datetime, timedelta

def generate_sample_weather_data():
    """Generate sample weather data for testing"""
    sample_data = []
    current_date = datetime.now()

    for i in range(7):  # Generate 7 days of data
        date = current_date + timedelta(days=i)
        sample_data.append({
            'date': date.strftime('%Y-%m-%d'),
            'temperature_2m_max': round(np.random.uniform(25, 35), 2),
            'temperature_2m_min': round(np.random.uniform(20, 25), 2),
            'precipitation_sum': round(np.random.uniform(0, 50), 2),
            'wind_speed_10m_max': round(np.random.uniform(5, 15), 2),
            'wind_gusts_10m_max': round(np.random.uniform(10, 25), 2),
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
        elif rain_prob > 0.3:
            weather_type = 'LIGHT_RAIN'
        elif temp > 32:
            weather_type = 'HOT'
        elif temp < 22:
            weather_type = 'COOL'
        else:
            weather_type = 'FAIR'

        results.append({
            'date': day['date'],
            'temperature': round(temp, 1),
            'rainfall_probability': round(rain_prob, 2),
            'wind_speed': round(day['wind_speed_10m_max'], 1),
            'weather_type': weather_type
        })
    return results

def predict_weather():
    try:
        # Generate sample data since model files aren't available
        input_data = generate_sample_weather_data()
        print("Using generated sample data:", json.dumps(input_data, indent=2))

        # Process the data
        predictions = process_weather_data(input_data)
        print(json.dumps(predictions, indent=2))
        return predictions

    except Exception as e:
        print(f"Error making prediction: {str(e)}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    predict_weather()