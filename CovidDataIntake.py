# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import csv

state = input("Enter state: ")

df = pd.read_parquet("https://pandemicdatalake.blob.core.windows.net/public/curated/covid-19/bing_covid-19_data/latest/bing_covid-19_data.parquet")

df_st = df.loc[df['admin_region_1'] == state]
df_st = df_st.loc[df_st['admin_region_2'].isnull()]

df_st = df_st.sort_values(by = 'updated', ascending=False)

cases = df_st['confirmed_change'].head(30)

filename = "covid_last_30.csv"

with open(filename, 'w') as csvfile:
	csvwriter = csv.writer(csvfile)
	for index,value in cases.items():
		csvwriter.writerow([value])
		





