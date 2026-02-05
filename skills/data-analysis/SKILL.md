---
name: data-analysis
description: Analyze datasets, generate insights, create visualizations, and perform statistical analysis. Use when working with data, spreadsheets, or need business intelligence.
license: Apache-2.0
metadata:
  author: agent-skill-team
  version: "1.0"
---

# Data Analysis Skill

## When to use this skill
Use this skill when you need to analyze datasets, extract insights from data, create reports, or perform statistical analysis. Works with CSV files, spreadsheets, JSON data, and database exports.

## How to analyze datasets

### Basic Data Analysis
1. **Load data**: Accept various data formats (CSV, Excel, JSON)
2. **Profile**: Generate data quality report and statistics
3. **Analyze**: Perform statistical analysis and identify patterns
4. **Visualize**: Create charts and graphs for insights
5. **Report**: Generate comprehensive analysis summary

### Parameters
- `data_source` (required): Path to data file or data URL
- `analysis_type` (optional): Type of analysis - ["descriptive", "predictive", "diagnostic", "prescriptive"] (default: "descriptive")
- `target_column` (optional): Column to analyze for specific insights
- `output_format` (optional): Report format - ["json", "html", "pdf"] (default: "json")
- `visualizations` (optional): Array of chart types - ["histogram", "scatter", "line", "bar", "heatmap"]

### Example
```
Request: "Analyze sales data for trends and insights"
Parameters: {
  "data_source": "/data/sales_2023.csv",
  "analysis_type": "descriptive",
  "target_column": "revenue",
  "visualizations": ["line", "bar", "scatter"],
  "output_format": "html"
}
```

## Analysis Types

### Descriptive Analysis
- **Summary statistics**: Mean, median, mode, standard deviation
- **Data distribution**: Histograms, box plots, density plots
- **Correlation analysis**: Heatmaps, correlation matrices
- **Trend analysis**: Time series patterns, seasonal trends
- **Outlier detection**: Statistical methods for anomaly identification

### Predictive Analysis
- **Regression**: Linear, polynomial, logistic regression
- **Time series**: ARIMA, exponential smoothing
- **Classification**: Decision trees, random forests, neural networks
- **Clustering**: K-means, hierarchical clustering

### Diagnostic Analysis
- **Data quality**: Missing values, duplicates, inconsistencies
- **Pattern detection**: Seasonal patterns, cyclical trends
- **Anomaly detection**: Statistical outlier identification
- **Data profiling**: Column statistics, data types, cardinality

## Data Format Support

### Input Formats
- **CSV**: Comma-separated values with various delimiters
- **Excel**: .xlsx, .xls with multiple sheets
- **JSON**: Nested JSON structures and arrays
- **Parquet**: Columnar storage format
- **Database**: SQL query results and connections

### Output Formats
- **JSON**: Structured data with metadata
- **HTML**: Interactive reports with embedded charts
- **PDF**: Professional reports with visualizations
- **CSV**: Processed data tables

## Visualization Options

### Chart Types
- **Histogram**: Data distribution analysis
- **Scatter plot**: Correlation and relationships
- **Line chart**: Time series and trends
- **Bar chart**: Categorical comparisons
- **Heatmap**: Correlation matrices
- **Box plot**: Statistical summaries
- **Pie chart**: Proportional analysis

### Advanced Visualizations
- **Interactive dashboards**: Multi-view data exploration
- **Geospatial maps**: Location-based data visualization
- **Network graphs**: Relationship mapping
- **3D plots**: Multi-dimensional analysis

## Statistical Methods

### Descriptive Statistics
- Central tendency: Mean, median, mode
- Dispersion: Variance, standard deviation, range
- Distribution: Skewness, kurtosis
- Percentiles: Quartiles, deciles, percentiles

### Hypothesis Testing
- **T-tests**: Compare means between groups
- **Chi-square**: Categorical variable relationships
- **ANOVA**: Multiple group comparisons
- **Correlation tests**: Pearson, Spearman correlation

## Scripts and Tools

### `scripts/data-cleaner.py`
Automated data cleaning and preprocessing:
- Remove duplicates and handle missing values
- Normalize and standardize data formats
- Detect and handle outliers

### `scripts/statistical-analyzer.py`
Comprehensive statistical analysis:
- Descriptive and inferential statistics
- Hypothesis testing and confidence intervals
- Effect size calculations

### `scripts/visualizer.py`
Data visualization generation:
- Chart creation with matplotlib/seaborn/plotly
- Interactive dashboards
- Custom styling and themes

### `scripts/data-validator.py`
Data quality assessment:
- Schema validation
- Data type checking
- Consistency verification

## Machine Learning Integration

### Preprocessing
- Feature scaling and normalization
- Encoding categorical variables
- Dimensionality reduction (PCA, t-SNE)
- Feature engineering

### Model Building
- **Supervised**: Regression, classification algorithms
- **Unsupervised**: Clustering, dimensionality reduction
- **Time series**: Forecasting models
- **Ensemble**: Random forests, gradient boosting

### Model Evaluation
- Cross-validation strategies
- Performance metrics (accuracy, precision, recall, F1)
- Feature importance analysis
- Model interpretability

## Report Generation

### Executive Summary
- Key findings and insights
- Business implications
- Recommendations and next steps

### Technical Details
- Methodology and assumptions
- Statistical significance tests
- Confidence intervals and margins of error

### Data Appendix
- Data sources and quality assessment
- Variable definitions and coding
- Limitations and assumptions

## Performance Optimization

### Large Datasets
- Chunked processing for memory efficiency
- Parallel processing for CPU utilization
- Lazy loading for I/O optimization

### Caching Strategy
- Intermediate result caching
- Computed statistics memoization
- Visualization layer caching

This skill provides comprehensive data analysis capabilities for business intelligence, research, and decision-making support.