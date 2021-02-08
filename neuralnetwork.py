import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import tensorflow as tf

from tensorflow import keras
from tensorflow.keras import layers

from memory_profiler import memory_usage
import time

# number of iterations in traning
EPOCHS = 10
# learning rate
LEARNING_RATE = 0.1
# stores predicted values in array
test_predictions = np.empty(0)
# stores training error
train_loss = 0.0

# build model for artificial neural network
def build_model(train_dataset, neurons):
    # print number of neurons
    print('Neurons: ',neurons)
    # create model with 3 layers
    # input layer, hidden layer with given number of neurons and output layer
    model = keras.Sequential([
        # creating hidden layer and input layer
        # relu computes f(x)=max(0,x)
        layers.Dense(neurons, activation='relu', input_shape=[len(train_dataset.keys())]),
        # output layer
        layers.Dense(1)
    ])
    # Optimizer that implements the Adam algorithm
    optimizer = tf.keras.optimizers.Adam(LEARNING_RATE)
    # config the model with losses and metrics
    # loss is measured in mse
    model.compile(loss='mse',optimizer=optimizer,metrics=['mae', 'mse'])
    return model

def train_model(model,normed_train_data,train_labels):
    # train the model
    history = model.fit(normed_train_data, train_labels,epochs=EPOCHS, validation_split = 0.2, verbose=1)
    # assigns traing error
    training_loss = history.history['loss']
    global train_loss
    train_loss = np.mean(np.abs(training_loss))

def test_model(model,normed_test_data):
    # test the model by predicting output for test set
    # use the model to do prediction
    global test_predictions
    test_predictions = model.predict(normed_test_data).flatten()

def main():
    # main function

    # path to dataset
    dataset_path = "dataset-shuffled.csv"
    # read dataset from csv file
    dataset = pd.read_csv(dataset_path)

    # spilt dataset into two sets: taining set and testing set
    train_dataset = dataset.sample(frac=0.7,random_state=0)
    test_dataset = dataset.drop(train_dataset.index)

    # get statistcs like standard deviation and mean for training set
    train_stats = train_dataset.describe()
    # remove security column
    train_stats.pop("security")
    train_stats = train_stats.transpose()
    # print(train_stats)

    # remove security column
    train_labels = train_dataset.pop('security')
    test_labels = test_dataset.pop('security')

    # noramalize data in traning and testing set
    normed_train_data = (train_dataset - train_stats['mean']) / train_stats['std']
    normed_test_data =  (test_dataset - train_stats['mean']) / train_stats['std']

    # array for number of neurons
    no_neurons = np.empty(0)
    # array for time taken to train model for corresponding number of neurons
    time_test = np.empty(0)
    # array for memory used to train model for corresponding number of neurons
    mem_usage_test = np.empty(0)
    # array for mean absolute error during train model for corresponding  number of neurons
    mae_test = np.empty(0)
    # array for time taken to test model for corresponding number of neurons
    time_train = np.empty(0)
    # array for memory used to test model for corresponding number of neurons
    mem_usage_train = np.empty(0)
    # array for mean absolute error during test model for corresponding  number of neurons
    mae_train = np.empty(0)
    # initial number of neurons
    i = 3
    # loop for 5 times
    # double the number of neurons in every loop
    for x in range(1, 10):
        # add current number of neurons to array
        no_neurons = np.append(no_neurons,[i])
        # build model with current number of neurons
        model = build_model(train_dataset,i)
        # print model summary
        print(model.summary())

        # start timer
        st_time=time.time()
        # call function to train model and profile memory usage
        mem_usage = memory_usage((train_model,(model,normed_train_data,train_labels)))
        # stop timer
        time_elapsed=time.time()-st_time

        # add total time taken to train model to array
        time_train = np.append(time_train,[time_elapsed])
        # add maximum memory used to train model to array
        mem_usage_train = np.append(mem_usage_train,[max(mem_usage)])
        # print time taken and memory used
        print('Total time taken to train: ',time_elapsed)
        # print('Memory used to train: %s' % mem_usage)
        print('Maximum memory used to train: %s' % max(mem_usage))
        # add mean absolute error caused during training to array
        mae_train = np.append(mae_train,[train_loss])
        # print error
        print("Training set Mean Abs Error: {:5.2f} security".format(train_loss))

        # start timer
        st_time=time.time()
        # call function to test model and profile memory usage
        mem_usage = memory_usage((test_model,(model,normed_test_data)))
        # stop timer
        time_elapsed=time.time()-st_time

        # add total time taken to test model to array
        time_test = np.append(time_test,[time_elapsed])
        # add maximum memory used to test model to array
        mem_usage_test = np.append(mem_usage_test,[max(mem_usage)])
        # print time taken and memory used
        print('Total time taken to test: ',time_elapsed)
        # print('Memory usage: %s' % mem_usage)
        print('Maximum memory used to test: %s' % max(mem_usage))
        # add mean absolute error caused during training to array
        error = np.mean(np.abs(test_predictions - test_labels))
        mae_test = np.append(mae_test,error)
        # print error
        print("Testing set Mean Abs Error: {:5.2f} security".format(error))

        # double the number of neurons
        i = i * 2

    # plot graphs for time taken, memory consumption and error during training model against number of neurons
    # graph for time taken against number of neurons
    plt.subplot(3, 1, 1)
    plt.plot(no_neurons,time_train)
    plt.ylabel('Time (s)')
    # graph for memory consumption against number of neurons
    plt.subplot(3, 1, 2)
    plt.plot(no_neurons, mem_usage_train)
    plt.ylabel('Memory Usage (mb)')
    # graph for error against number of neurons
    plt.subplot(3, 1, 3)
    plt.plot(no_neurons,mae_train)
    plt.ylabel('MAE')
    plt.xlabel('neurons')
    plt.savefig("train.png")

    plt.show()

    # plot graphs for time taken, memory consumption and error during testing model against number of neurons
    # graph for time taken against number of neurons
    plt.subplot(3, 1, 1)
    plt.plot(no_neurons,time_test)
    plt.ylabel('Time (s)')

    # graph for memory consumption against number of neurons
    plt.subplot(3, 1, 2)
    plt.plot(no_neurons, mem_usage_test)
    plt.ylabel('Memory Usage (mb)')
    # graph for error against number of neurons
    plt.subplot(3, 1, 3)
    plt.plot(no_neurons,mae_test)
    plt.ylabel('MAE')
    plt.xlabel('neurons')
    plt.savefig("test.png")
    plt.show()

if __name__ == '__main__':
    main()
