import random


def generate_random_bigint_in_range(min_val, max_val):
    return random.randint(min_val, max_val)


min_value = 1  # 最小值
max_value = 2 ** 128  # 最大值

result = generate_random_bigint_in_range(min_value, max_value)
print("随机 BigInteger 在指定范围内:", result)
