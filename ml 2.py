import pandas as pd
import numpy as np
import sklearn
from sklearn import linear_model,preprocessing
from sklearn.neighbors import KNeighborsClassifier


data=pd.read_csv("car.data")
print(data.head())
le=preprocessing.LabelEncoder()

buying=le.fit_transform(list(data["buying"]))
maint=le.fit_transform(list(data["maint"]))
door=le.fit_transform(list(data["door"]))
persons=le.fit_transform(list(data["persons"]))
lug_boot=le.fit_transform(list(data["lug_boot"]))
safety=le.fit_transform(list(data["safety"]))
cls=le.fit_transform(list(data["class"]))

x=list(zip(buying,maint,door,persons,lug_boot,safety))
y=list(cls)
print(y)
x_train,x_test,y_train,y_test=sklearn.model_selection.train_test_split(x,y,test_size=.1)
print(y_test)
print(y_train)
model=KNeighborsClassifier(n_neighbors=9)

model.fit(x_train,y_train)
acc=model.score(x_test,y_test)
print(acc)
predict=model.predict(x_test)
name=['unacc','acc','good','vgood']
for x in range(len(predict)):
   print(name[predict[x]],name[y_test[x]])
