# Decision Record: Use Debounce for API Calls

## Context
In our project, we frequently make API calls based on user input. Rapid user interactions can lead to excessive API requests, which can overwhelm our server and degrade performance.

## Problem
The main issue is that without a mechanism to limit the frequency of API calls, we risk sending too many requests in a short period, leading to potential server overload and increased latency in responses.

## Decision
We have decided to implement a debounce mechanism for our API calls. This will ensure that API requests are only sent after a specified delay following the last user interaction, effectively reducing the number of calls made.

## Consequences
By using debounce, we will improve the performance of our application by minimizing unnecessary API calls. This will lead to better resource management on the server side and a smoother user experience. However, there may be a slight delay in the response to user actions, which we need to communicate to users.

## Alternatives Considered
1. Throttling: This approach limits the number of calls to a fixed rate but does not address the issue of rapid user interactions effectively.
2. No mechanism: Continuing without any limits would lead to potential performance issues and a poor user experience.