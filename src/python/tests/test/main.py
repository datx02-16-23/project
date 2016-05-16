# merge sort
def merge_sort(array):
    merge_sort_r(array, 0, len(array) -1)

# merge sort recursive (used by merge_sort)
def merge_sort_r(array, first, last):
    if first < last:
        sred = (first + last)/2
        merge_sort_r(array, first, sred)
        merge_sort_r(array, sred + 1, last)
        merge(array, first, last, sred)

# merge (used by merge_sort_r)
def merge(array, first, last, sred):
    helper_list = []
    i = first
    j = sred + 1
    while i <= sred and j <= last:
        if array [i] <= array [j]:
            helper_list.append(array[i])
            i += 1
        else:
            helper_list.append(array [j])
            j += 1
    while i <= sred:
        helper_list.append(array[i])
        i +=1
    while j <= last:
        helper_list.append(array[j])
        j += 1
    for k in range(0, last - first + 1):
        array[first + k] = helper_list [k]

from random import random as r
rng = 100
size = 20
vec = [int(r() * rng) for i in range(0,size)]
merge_sort(vec)