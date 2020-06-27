import sklearn
from sklearn import svm
from sklearn import datasets


can=datasets.load_breast_cancer()
x=can.data
y=can.target

x_train,x_test,y_train,y_test=sklearn.model_selection.train_test_split(x,y,test_size=.1)

model=svm.SVC(kernel='linear')
model.fit(x_train,y_train)

predict=model.predict(x_test)
acc=model.score(x_test,y_test)
print(acc)
for x in range(len(predict)):
    print(predict[x],x_test[x],y_test[x])
