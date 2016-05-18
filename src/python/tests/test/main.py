# y is captured but not replaced with "a" or "b"
def test_(y):
    return y

def test(x):
    x = [1,2,3]
    return test_(x)

a = None
test(a)

b = None
test(b)