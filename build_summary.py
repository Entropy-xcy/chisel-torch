import os
from unittest import result

if __name__ == "__main__":
    log_dir = "cell_out"
    result_dict = {}
    for log_fn in os.listdir(log_dir):
        log_f = os.path.join(log_dir, log_fn)
        log_idx = log_fn.split(".")[0]
        log = open(log_f, "r").read()
        if log not in result_dict.keys():
            result_dict[log] = [log_idx]
        else:
            result_dict[log].append(log_idx)
    
    for k, v in result_dict.items():
        print(k, len(v))
