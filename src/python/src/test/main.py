from random import random

def rand_array(size,rng):
	array = []
	for i in range(0,size):
		array.append(int(random()*rng))
	return array

x = 10
a = rand_array(x,50)

for i in range(0,len(a)):
	for j in range(0,len(a) - 1):
		if a[j] > a[j+1]:
			tmp = a[j]
			a[j] = a[j+1]
			a[j+1] = tmp