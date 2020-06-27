import pandas as pd
import numpy as np
import sklearn
from sklearn import linear_model,preprocessing
from sklearn.linear_model import LogisticRegression
data=pd.read_csv("HR_comma_sep.csv")
#print(data.head())
data=data[["satisfaction_level","left","salary"]]
dumi=pd.get_dummies(data.salary)
#print(dumi.head())


merged=pd.concat([data,dumi],axis="columns")
print(merged.head())
final=merged.drop(["salary"],axis="columns")
print(final.head())
x=np.array(final.drop(["left"],1))
y=np.array(final["left"])


x_train,x_test,y_train,y_test=sklearn.model_selection.train_test_split(x,y,test_size=0.1)

linear= LogisticRegression()

linear.fit(x_train,y_train)
acc=linear.score(x_test,y_test)
print(acc)
predictions=linear.predict(x_test)
for i in range(len(predictions)):
    print(predictions[i],y_test[i])


