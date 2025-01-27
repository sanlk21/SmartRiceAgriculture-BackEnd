import sys
import numpy as np
import tensorflow as tf
from tensorflow import keras
from sklearn.preprocessing import StandardScaler
import joblib
from pathlib import Path

# train_model.py
def create_and_save_model():
    X = np.random.rand(1000, 7)
    y = np.sum(X, axis=1) + np.random.normal(0, 0.1, 1000)

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    model = keras.Sequential([
        keras.layers.Dense(64, activation='relu', input_shape=(7,)),
        keras.layers.Dense(32, activation='relu'),
        keras.layers.Dense(1)
    ])

    model.compile(optimizer='adam', loss='mse')
    model.fit(X_scaled, y, epochs=50, batch_size=32, validation_split=0.2, verbose=1)

    paths = {
        'model': Path('weather_prediction_model.keras'),
        'scaler': Path('weather_scalers.pkl')
    }

    model.save(str(paths['model']))
    joblib.dump(scaler, str(paths['scaler']))

if __name__ == "__main__":
    create_and_save_model()