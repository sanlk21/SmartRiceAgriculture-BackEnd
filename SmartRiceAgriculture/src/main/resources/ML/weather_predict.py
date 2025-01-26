# weather_predict.py
import sys
import numpy as np
import tensorflow as tf
import joblib
import json

def predict_weather(input_string):
    model = tf.keras.models.load_model('weather_prediction_model.keras')
    scaler = joblib.load('weather_scalers.pkl')

    clean_string = input_string.replace("'", '"')
    data = np.array(json.loads(clean_string)).reshape(1, -1)

    scaled_data = scaler.transform(data)
    prediction = model.predict(scaled_data, verbose=0)
    return float(prediction[0][0])

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python weather_predict.py '[0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1]'")
        sys.exit(1)

    try:
        result = predict_weather(sys.argv[1])
        print(f"Predicted value: {result:.4f}")
    except Exception as e:
        print(f"Error: {str(e)}")